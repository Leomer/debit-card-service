package com.bank.debitcard.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Listens to the debit-payment-results topic. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResultListener {

    private final PaymentResultHandler handler;

    @KafkaListener(topics = "debit-payment-results")
    public void onMessage(String message) {
        handler.handle(message)
                .subscribe(v -> { }, e -> log.error("Could not process {}: {}", message, e.toString()));
    }
}
