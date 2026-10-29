package com.bank.debitcard.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/** Debit card linked to one or more bank accounts. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "debit_cards")
public class DebitCard {

    @Id
    private String id;

    @Indexed(unique = true)
    private String cardNumber;

    /** Document number of the card holder. */
    private String customerDocument;

    /** Account numbers linked to the card (order = order of association). */
    private List<String> accountNumbers;

    /** Account that receives the charges and credits. Must be one of accountNumbers. */
    private String primaryAccount;
}