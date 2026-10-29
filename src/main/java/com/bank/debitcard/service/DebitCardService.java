package com.bank.debitcard.service;

import com.bank.debitcard.dto.DebitCardDto;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/** Debit card operations. */
public interface DebitCardService {
    Single<DebitCardDto> create(DebitCardDto dto);
    Flowable<DebitCardDto> findAll();
    Single<DebitCardDto> findByCardNumber(String cardNumber);
    Flowable<DebitCardDto> findByCustomer(String customerDocument);
    Single<DebitCardDto> update(String cardNumber, DebitCardDto dto);
    Single<DebitCardDto> changePrimaryAccount(String cardNumber, String accountNumber);
    Completable delete(String cardNumber);
}