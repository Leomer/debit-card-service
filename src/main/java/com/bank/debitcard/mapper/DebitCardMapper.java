package com.bank.debitcard.mapper;

import com.bank.debitcard.dto.DebitCardDto;
import com.bank.debitcard.model.DebitCard;

import java.util.ArrayList;

/** Converts between DebitCard entities and DTOs. */
public final class DebitCardMapper {

    private DebitCardMapper() {
    }

    public static DebitCardDto toDto(DebitCard e) {
        return DebitCardDto.builder()
                .id(e.getId())
                .cardNumber(e.getCardNumber())
                .customerDocument(e.getCustomerDocument())
                .accountNumbers(e.getAccountNumbers())
                .primaryAccount(e.getPrimaryAccount())
                .build();
    }

    public static DebitCard toEntity(DebitCardDto d) {
        return DebitCard.builder()
                .id(d.getId())
                .cardNumber(d.getCardNumber())
                .customerDocument(d.getCustomerDocument())
                .accountNumbers(d.getAccountNumbers() == null
                        ? new ArrayList<>() : new ArrayList<>(d.getAccountNumbers()))
                .primaryAccount(d.getPrimaryAccount())
                .build();
    }
}