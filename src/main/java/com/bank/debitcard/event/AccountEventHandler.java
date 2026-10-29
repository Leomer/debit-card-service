package com.bank.debitcard.event;

import com.bank.debitcard.model.KnownAccount;
import com.bank.debitcard.repository.KnownAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/** Applies account events to the local copy of accounts. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AccountEventHandler {

    private final KnownAccountRepository repository;

    /** Handles CREATED|number|customer|type and DELETED|number. */
    public Mono<Void> handle(String message) {
        String[] parts = message.split("\\|", -1);
        switch (parts[0]) {
            case "CREATED" -> {
                log.info("Account created event: {}", parts[1]);
                return repository.save(KnownAccount.builder()
                        .accountNumber(parts[1])
                        .customerDocument(parts[2])
                        .accountType(parts[3])
                        .build()).then();
            }
            case "DELETED" -> {
                log.info("Account deleted event: {}", parts[1]);
                return repository.deleteById(parts[1]);
            }
            default -> {
                log.warn("Unknown account event ignored: {}", message);
                return Mono.empty();
            }
        }
    }
}