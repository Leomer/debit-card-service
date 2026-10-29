package com.bank.debitcard.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Payment made with a debit card, charged to the card's primary account. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "debit_payments")
public class DebitPayment {

    @Id
    private String id;

    private String cardNumber;

    /** Primary account at the moment of the payment. */
    private String accountNumber;

    private BigDecimal amount;

    private PaymentStatus status;

    /** Failure reason, when status is FAILED. */
    private String reason;

    /** Account balance after the charge, when status is COMPLETED. */
    private BigDecimal balanceAfter;

    private LocalDateTime createdAt;
}