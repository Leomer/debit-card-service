package com.bank.debitcard.dto;

import com.bank.debitcard.model.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Debit payment returned by the API. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebitPaymentDto {
    private String paymentId;
    private String cardNumber;
    private String accountNumber;
    private BigDecimal amount;
    private PaymentStatus status;
    private String reason;
    private BigDecimal balanceAfter;
    private LocalDateTime createdAt;
}