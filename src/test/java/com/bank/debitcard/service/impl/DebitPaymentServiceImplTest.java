package com.bank.debitcard.service.impl;

import com.bank.debitcard.event.PaymentEventPublisher;
import com.bank.debitcard.exception.NotFoundException;
import com.bank.debitcard.model.DebitCard;
import com.bank.debitcard.model.DebitPayment;
import com.bank.debitcard.model.PaymentStatus;
import com.bank.debitcard.repository.DebitCardRepository;
import com.bank.debitcard.repository.DebitPaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for {@link DebitPaymentServiceImpl}. */
@ExtendWith(MockitoExtension.class)
class DebitPaymentServiceImplTest {

    @Mock
    private DebitCardRepository cardRepository;

    @Mock
    private DebitPaymentRepository paymentRepository;

    @Mock
    private PaymentEventPublisher publisher;

    @InjectMocks
    private DebitPaymentServiceImpl service;

    @Test
    void requestPaymentShouldSavePendingPaymentOnPrimaryAccountAndPublish() {
        DebitCard card = DebitCard.builder().cardNumber("DC1").customerDocument("123")
                .accountNumbers(List.of("A", "B")).primaryAccount("B").build();
        when(cardRepository.findByCardNumber("DC1")).thenReturn(Mono.just(card));
        when(paymentRepository.save(any(DebitPayment.class)))
                .thenAnswer(i -> {
                    DebitPayment p = i.getArgument(0);
                    p.setId("P1");
                    return Mono.just(p);
                });

        service.requestPayment("DC1", new BigDecimal("25.50"))
                .test()
                .assertValue(p -> "P1".equals(p.getPaymentId())
                        && "B".equals(p.getAccountNumber())
                        && p.getStatus() == PaymentStatus.PENDING)
                .assertComplete();

        verify(publisher).requested(any(DebitPayment.class));
    }

    @Test
    void requestPaymentShouldFailWhenCardDoesNotExist() {
        when(cardRepository.findByCardNumber("X")).thenReturn(Mono.empty());

        service.requestPayment("X", BigDecimal.TEN).test().assertError(NotFoundException.class);

        verify(paymentRepository, never()).save(any());
        verify(publisher, never()).requested(any());
    }

    @Test
    void findPaymentShouldFailWhenMissing() {
        when(paymentRepository.findById("X")).thenReturn(Mono.empty());

        service.findPayment("X").test().assertError(NotFoundException.class);
    }
}
