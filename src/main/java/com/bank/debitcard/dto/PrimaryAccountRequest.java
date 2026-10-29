package com.bank.debitcard.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Request to change the primary account of a card. */
@Data
public class PrimaryAccountRequest {

    @NotBlank(message = "accountNumber is required")
    private String accountNumber;
}