# debit-card-service

Debit cards microservice. A card is linked to one or more accounts of its
holder, and one of them is the **primary account**, the one that is charged.
Spring Boot, WebFlux, RxJava 3, MongoDB, Kafka.

## Design decisions
- **Own database** (`debitcard_db`).
- **No REST calls to other services** (Part III requirement for new services).
  Everything it needs from the bank arrives through Kafka events.
- **Local copy of accounts** (`known_accounts`), fed by the `account-events`
  topic. Used to check that every account of a card exists and belongs to the
  holder.
- **Payments are asynchronous.** The request is stored as PENDING and a request
  event is published; transaction-service performs the withdrawal on the
  primary account and publishes the result.
- **Publishes its cards** to `debit-card-events`, so yanki-service can keep its
  own copy.

## Requirements
- JDK 17, Maven, MongoDB, Kafka (:9092)
- config-server (:8888), eureka-server (:8761)
- account-service and transaction-service running, to feed and process events

## Run
1. Start MongoDB, Kafka, config-server and eureka-server
2. `mvn spring-boot:run`
- Port: 8087 (from config-repo)
- Database: `debitcard_db`
- Swagger UI: http://localhost:8087/swagger-ui.html
- Through the gateway: http://localhost:8080/debit-cards/...

## Business rules
| Rule | Detail |
|---|---|
| Linked accounts | Every account must exist (local copy) and belong to the card holder |
| Primary account | Defaults to the first account; must be one of the linked accounts |
| Update | If the primary account is unlinked, the first remaining account becomes primary |
| Payment | Charged to the primary account at the moment of the payment |

## Endpoints
| Method | Path | Description |
|---|---|---|
| POST | /debit-cards | Create a card |
| GET | /debit-cards | List cards |
| GET | /debit-cards/{cardNumber} | Find a card |
| GET | /debit-cards/customer/{customerDocument} | Cards of a customer |
| PUT | /debit-cards/{cardNumber} | Update linked accounts |
| PATCH | /debit-cards/{cardNumber}/primary-account | Change the primary account |
| DELETE | /debit-cards/{cardNumber} | Delete a card (ADMIN) |
| POST | /debit-cards/{cardNumber}/payments | Pay with the card (202, asynchronous) |
| GET | /debit-cards/payments/{paymentId} | Payment status |

Payment states: `PENDING`, `COMPLETED` (with `balanceAfter`), `FAILED` (with `reason`).

## Kafka topics
| Topic | Direction | Message |
|---|---|---|
| account-events | consumes | `CREATED\|number\|customer\|type`, `DELETED\|number` |
| debit-payment-requests | publishes | `REQUESTED\|paymentId\|card\|account\|amount` |
| debit-payment-results | consumes | `COMPLETED\|paymentId\|balanceAfter`, `FAILED\|paymentId\|reason` |
| debit-card-events | publishes | `CARD\|card\|customer\|primaryAccount`, `CARD_DELETED\|card` |

Messages are plain text separated by `|`, so the services do not depend on a
JSON library version.

## Last 10 movements report
`GET /reports/products/{accountNumber}/last-movements` (report-service) on the
card's primary account. There is no separate history per card.

## Documentation
- OpenAPI contract: `docs/openapi.yaml`
- Sequence diagram: `docs/sequence-debit-payment.puml`
- Architecture diagram: see the `bank-docs` repository

## Known limitations
- **At-least-once delivery:** if Kafka redelivers a request, the account could be
  charged twice. The fix is idempotency using `paymentId` in transaction-service.
- **Lost request:** if publishing the request fails, the payment stays PENDING
  forever. An outbox or a scheduled retry would fix it.
- **Stale local copies:** accounts created before the consumer existed are not in
  `known_accounts`, and a lost DELETED event leaves a stale account.
- The same result can be applied only once: later duplicates are ignored.

## Tests
`mvn clean test`. Coverage report: `target/site/jacoco/index.html`.