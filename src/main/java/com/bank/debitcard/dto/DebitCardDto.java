package com.bank.debitcard.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Data transfer object for debit cards. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DebitCardDto {

    private String id;

    private String cardNumber;

    @NotBlank(message = "customerDocument is required")
    private String customerDocument;

    @NotEmpty(message = "at least one account is required")
    private List<String> accountNumbers;

    /** Optional on creation: defaults to the first account. */
    private String primaryAccount;
}