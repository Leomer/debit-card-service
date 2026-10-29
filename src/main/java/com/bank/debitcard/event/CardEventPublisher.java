package com.bank.debitcard.event;

import com.bank.debitcard.dto.DebitCardDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** Publishes debit card events. Failures are logged, never thrown. */
@Slf4j
@Component
@RequiredArgsConstructor
public class CardEventPublisher {

    public static final String TOPIC = "debit-card-events";

    private final KafkaTemplate<String, String> kafkaTemplate;

    /** Format: CARD|cardNumber|customerDocument|primaryAccount. */
    public void cardSaved(DebitCardDto card) {
        send(card.getCardNumber(), String.join("|", "CARD", card.getCardNumber(),
                card.getCustomerDocument(), card.getPrimaryAccount()));
    }

    /** Format: CARD_DELETED|cardNumber. */
    public void cardDeleted(String cardNumber) {
        send(cardNumber, "CARD_DELETED|" + cardNumber);
    }

    private void send(String key, String message) {
        try {
            kafkaTemplate.send(TOPIC, key, message).whenComplete((result, error) -> {
                if (error != null) {
                    log.error("Could not publish {}: {}", message, error.toString());
                } else {
                    log.info("Card event published: {}", message);
                }
            });
        } catch (Exception e) {
            log.error("Could not publish {}: {}", message, e.toString());
        }
    }
}