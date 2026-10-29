package com.bank.debitcard.event;

import com.bank.debitcard.model.PaymentStatus;
import com.bank.debitcard.repository.DebitPaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/** Applies payment results (COMPLETED / FAILED) to the stored payments. */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResultHandler {

    private final DebitPaymentRepository repository;

    /** Handles COMPLETED|paymentId|balanceAfter and FAILED|paymentId|reason. */
    public Mono<Void> handle(String message) {
        String[] parts = message.split("\\|", 3);
        if (parts.length < 3) {
            log.warn("Malformed payment result ignored: {}", message);
            return Mono.empty();
        }
        return repository.findById(parts[1])
                .flatMap(payment -> {
                    if (payment.getStatus() != PaymentStatus.PENDING) {
                        return Mono.empty(); // duplicate result: ignore
                    }
                    if ("COMPLETED".equals(parts[0])) {
                        payment.setStatus(PaymentStatus.COMPLETED);
                        payment.setBalanceAfter(new BigDecimal(parts[2]));
                    } else {
                        payment.setStatus(PaymentStatus.FAILED);
                        payment.setReason(parts[2]);
                    }
                    log.info("Payment {} is now {}", payment.getId(), payment.getStatus());
                    return repository.save(payment);
                })
                .then();
    }
}