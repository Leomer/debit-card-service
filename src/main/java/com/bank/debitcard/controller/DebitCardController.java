package com.bank.debitcard.controller;

import com.bank.debitcard.dto.DebitCardDto;
import com.bank.debitcard.dto.PrimaryAccountRequest;
import com.bank.debitcard.service.DebitCardService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** REST endpoints for debit cards. */
@RestController
@RequestMapping("/debit-cards")
@RequiredArgsConstructor
public class DebitCardController {

    private final DebitCardService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Single<DebitCardDto> create(@Valid @RequestBody DebitCardDto dto) {
        return service.create(dto);
    }

    @GetMapping
    public Flowable<DebitCardDto> findAll() {
        return service.findAll();
    }

    @GetMapping("/{cardNumber}")
    public Single<DebitCardDto> findByCardNumber(@PathVariable String cardNumber) {
        return service.findByCardNumber(cardNumber);
    }

    @GetMapping("/customer/{customerDocument}")
    public Flowable<DebitCardDto> findByCustomer(@PathVariable String customerDocument) {
        return service.findByCustomer(customerDocument);
    }

    @PutMapping("/{cardNumber}")
    public Single<DebitCardDto> update(@PathVariable String cardNumber,
                                       @Valid @RequestBody DebitCardDto dto) {
        return service.update(cardNumber, dto);
    }

    @PatchMapping("/{cardNumber}/primary-account")
    public Single<DebitCardDto> changePrimaryAccount(@PathVariable String cardNumber,
                                                     @Valid @RequestBody PrimaryAccountRequest request) {
        return service.changePrimaryAccount(cardNumber, request.getAccountNumber());
    }

    @DeleteMapping("/{cardNumber}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Completable delete(@PathVariable String cardNumber) {
        return service.delete(cardNumber);
    }
}