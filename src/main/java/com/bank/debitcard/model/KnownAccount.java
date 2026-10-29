package com.bank.debitcard.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/** Local copy of an account announced by account-service. The id is the account number. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "known_accounts")
public class KnownAccount {

    @Id
    private String accountNumber;

    private String customerDocument;

    private String accountType;
}