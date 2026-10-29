package com.bank.debitcard.service;

import com.bank.debitcard.dto.DebitPaymentDto;
import io.reactivex.rxjava3.core.Single;

import java.math.BigDecimal;

/** Debit card payments (asynchronous, event driven). */
public interface DebitPaymentService {

    /** Registers a PENDING payment and asks transaction-service to process it. */
    Single<DebitPaymentDto> requestPayment(String cardNumber, BigDecimal amount);

    /** Current state of a payment. */
    Single<DebitPaymentDto> findPayment(String paymentId);
}
