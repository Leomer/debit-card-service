package com.bank.debitcard.repository;

import com.bank.debitcard.model.DebitCard;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Reactive repository for debit cards (derived queries only). */
public interface DebitCardRepository extends ReactiveMongoRepository<DebitCard, String> {
    Mono<DebitCard> findByCardNumber(String cardNumber);
    Flux<DebitCard> findByCustomerDocument(String customerDocument);
}