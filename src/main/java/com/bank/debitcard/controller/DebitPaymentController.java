package com.bank.debitcard.controller;

import com.bank.debitcard.dto.DebitPaymentDto;
import com.bank.debitcard.dto.PaymentRequest;
import com.bank.debitcard.service.DebitPaymentService;
import io.reactivex.rxjava3.core.Single;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** REST endpoints for debit card payments. */
@RestController
@RequestMapping("/debit-cards")
@RequiredArgsConstructor
public class DebitPaymentController {

    private final DebitPaymentService paymentService;

    @PostMapping("/{cardNumber}/payments")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Single<DebitPaymentDto> pay(@PathVariable String cardNumber,
                                       @Valid @RequestBody PaymentRequest request) {
        return paymentService.requestPayment(cardNumber, request.getAmount());
    }

    @GetMapping("/payments/{paymentId}")
    public Single<DebitPaymentDto> findPayment(@PathVariable String paymentId) {
        return paymentService.findPayment(paymentId);
    }
}