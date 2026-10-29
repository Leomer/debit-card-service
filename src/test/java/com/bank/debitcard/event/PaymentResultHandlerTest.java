package com.bank.debitcard.event;

import com.bank.debitcard.model.DebitPayment;
import com.bank.debitcard.model.PaymentStatus;
import com.bank.debitcard.repository.DebitPaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for {@link PaymentResultHandler}. */
@ExtendWith(MockitoExtension.class)
class PaymentResultHandlerTest {

    @Mock
    private DebitPaymentRepository repository;

    @InjectMocks
    private PaymentResultHandler handler;

    private DebitPayment payment(PaymentStatus status) {
        return DebitPayment.builder().id("P1").status(status).amount(BigDecimal.TEN).build();
    }

    @Test
    void completedResultShouldStoreBalance() {
        when(repository.findById("P1")).thenReturn(Mono.just(payment(PaymentStatus.PENDING)));
        when(repository.save(any(DebitPayment.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        handler.handle("COMPLETED|P1|90.50").block();

        verify(repository).save(argThat(p -> p.getStatus() == PaymentStatus.COMPLETED
                && new BigDecimal("90.50").compareTo(p.getBalanceAfter()) == 0));
    }

    @Test
    void failedResultShouldStoreReason() {
        when(repository.findById("P1")).thenReturn(Mono.just(payment(PaymentStatus.PENDING)));
        when(repository.save(any(DebitPayment.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        handler.handle("FAILED|P1|Insufficient funds").block();

        verify(repository).save(argThat(p -> p.getStatus() == PaymentStatus.FAILED
                && "Insufficient funds".equals(p.getReason())));
    }

    @Test
    void resultForAnAlreadyResolvedPaymentShouldBeIgnored() {
        when(repository.findById("P1")).thenReturn(Mono.just(payment(PaymentStatus.COMPLETED)));

        handler.handle("FAILED|P1|late duplicate").block();

        verify(repository, never()).save(any());
    }

    @Test
    void malformedMessageShouldBeIgnored() {
        handler.handle("garbage").block();

        verify(repository, never()).findById(any(String.class));
    }
}
