package com.bank.debitcard.service.impl;

import com.bank.debitcard.dto.DebitPaymentDto;
import com.bank.debitcard.event.PaymentEventPublisher;
import com.bank.debitcard.exception.NotFoundException;
import com.bank.debitcard.model.DebitCard;
import com.bank.debitcard.model.DebitPayment;
import com.bank.debitcard.model.PaymentStatus;
import com.bank.debitcard.repository.DebitCardRepository;
import com.bank.debitcard.repository.DebitPaymentRepository;
import com.bank.debitcard.service.DebitPaymentService;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Default implementation of {@link DebitPaymentService}. */
@Slf4j
@Service
@RequiredArgsConstructor
public class DebitPaymentServiceImpl implements DebitPaymentService {

    private final DebitCardRepository cardRepository;
    private final DebitPaymentRepository paymentRepository;
    private final PaymentEventPublisher publisher;

    @Override
    public Single<DebitPaymentDto> requestPayment(String cardNumber, BigDecimal amount) {
        log.info("Payment of {} requested with card {}", amount, cardNumber);
        return Maybe.fromPublisher(cardRepository.findByCardNumber(cardNumber))
                .switchIfEmpty(Single.<DebitCard>error(
                        new NotFoundException("Debit card not found: " + cardNumber)))
                .flatMap(card -> Single.fromPublisher(paymentRepository.save(DebitPayment.builder()
                        .cardNumber(card.getCardNumber())
                        .accountNumber(card.getPrimaryAccount())
                        .amount(amount)
                        .status(PaymentStatus.PENDING)
                        .createdAt(LocalDateTime.now())
                        .build())))
                .doOnSuccess(publisher::requested)
                .map(this::toDto);
    }

    @Override
    public Single<DebitPaymentDto> findPayment(String paymentId) {
        return Maybe.fromPublisher(paymentRepository.findById(paymentId))
                .switchIfEmpty(Single.<DebitPayment>error(
                        new NotFoundException("Payment not found: " + paymentId)))
                .map(this::toDto);
    }

    private DebitPaymentDto toDto(DebitPayment p) {
        return DebitPaymentDto.builder()
                .paymentId(p.getId()).cardNumber(p.getCardNumber())
                .accountNumber(p.getAccountNumber()).amount(p.getAmount())
                .status(p.getStatus()).reason(p.getReason())
                .balanceAfter(p.getBalanceAfter()).createdAt(p.getCreatedAt())
                .build();
    }
}
