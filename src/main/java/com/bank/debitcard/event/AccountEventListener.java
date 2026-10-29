package com.bank.debitcard.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Listens to the account-events topic. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountEventListener {

    private final AccountEventHandler handler;

    @KafkaListener(topics = "account-events")
    public void onMessage(String message) {
        handler.handle(message)
                .subscribe(v -> { }, e -> log.error("Could not process event {}: {}", message, e.toString()));
    }
}