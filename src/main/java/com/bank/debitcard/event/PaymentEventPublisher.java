package com.bank.debitcard.event;

import com.bank.debitcard.model.DebitPayment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** Publishes debit payment requests. Failures are logged, never thrown. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventPublisher {

    public static final String TOPIC = "debit-payment-requests";

    private final KafkaTemplate<String, String> kafkaTemplate;

    /** Format: REQUESTED|paymentId|cardNumber|accountNumber|amount. */
    public void requested(DebitPayment payment) {
        String message = String.join("|", "REQUESTED", payment.getId(), payment.getCardNumber(),
                payment.getAccountNumber(), payment.getAmount().toPlainString());
        try {
            kafkaTemplate.send(TOPIC, payment.getId(), message).whenComplete((result, error) -> {
                if (error != null) {
                    log.error("Could not publish {}: {}", message, error.toString());
                } else {
                    log.info("Payment request published: {}", message);
                }
            });
        } catch (Exception e) {
            log.error("Could not publish {}: {}", message, e.toString());
        }
    }
}