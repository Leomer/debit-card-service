package com.bank.debitcard.service.impl;

import com.bank.debitcard.dto.DebitCardDto;
import com.bank.debitcard.event.CardEventPublisher;
import com.bank.debitcard.exception.BusinessException;
import com.bank.debitcard.exception.NotFoundException;
import com.bank.debitcard.mapper.DebitCardMapper;
import com.bank.debitcard.model.DebitCard;
import com.bank.debitcard.repository.DebitCardRepository;
import com.bank.debitcard.repository.KnownAccountRepository;
import com.bank.debitcard.service.DebitCardService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Default implementation of {@link DebitCardService}. */
@Slf4j
@Service
@RequiredArgsConstructor
public class DebitCardServiceImpl implements DebitCardService {

    private final DebitCardRepository repository;
    private final KnownAccountRepository knownAccountRepository;
    private final CardEventPublisher cardEventPublisher;

    @Override
    public Single<DebitCardDto> create(DebitCardDto dto) {
        log.info("Creating debit card for customer {}", dto.getCustomerDocument());
        List<String> accounts = distinct(dto.getAccountNumbers());
        String primary = dto.getPrimaryAccount() == null ? accounts.get(0) : dto.getPrimaryAccount();
        if (!accounts.contains(primary)) {
            return Single.error(new BusinessException("Primary account must be one of the linked accounts"));
        }
        return validateAccounts(dto.getCustomerDocument(), accounts)
                .flatMap(valid -> {
                    DebitCard card = DebitCardMapper.toEntity(dto);
                    card.setId(null);
                    card.setCardNumber(newCardNumber());
                    card.setAccountNumbers(valid);
                    card.setPrimaryAccount(primary);
                    return Single.fromPublisher(repository.save(card));
                })
                .map(DebitCardMapper::toDto)
                .doOnSuccess(cardEventPublisher::cardSaved);
    }

    @Override
    public Flowable<DebitCardDto> findAll() {
        return Flowable.fromPublisher(repository.findAll()).map(DebitCardMapper::toDto);
    }

    @Override
    public Single<DebitCardDto> findByCardNumber(String cardNumber) {
        return findEntity(cardNumber).map(DebitCardMapper::toDto);
    }

    @Override
    public Flowable<DebitCardDto> findByCustomer(String customerDocument) {
        return Flowable.fromPublisher(repository.findByCustomerDocument(customerDocument))
                .map(DebitCardMapper::toDto);
    }

    @Override
    public Single<DebitCardDto> update(String cardNumber, DebitCardDto dto) {
        log.info("Updating debit card {}", cardNumber);
        return findEntity(cardNumber).flatMap(card -> {
            List<String> accounts = distinct(dto.getAccountNumbers());
            String primary = dto.getPrimaryAccount() == null ? card.getPrimaryAccount()
                    : dto.getPrimaryAccount();
            if (!accounts.contains(primary)) {
                primary = accounts.get(0); // the old primary was unlinked
            }
            String finalPrimary = primary;
            return validateAccounts(card.getCustomerDocument(), accounts)
                    .flatMap(valid -> {
                        card.setAccountNumbers(valid);
                        card.setPrimaryAccount(finalPrimary);
                        return Single.fromPublisher(repository.save(card));
                    });
        }).map(DebitCardMapper::toDto)
        .doOnSuccess(cardEventPublisher::cardSaved);
    }

    @Override
    public Single<DebitCardDto> changePrimaryAccount(String cardNumber, String accountNumber) {
        return findEntity(cardNumber).flatMap(card -> {
            if (!card.getAccountNumbers().contains(accountNumber)) {
                return Single.<DebitCard>error(new BusinessException(
                        "Account is not linked to this card: " + accountNumber));
            }
            card.setPrimaryAccount(accountNumber);
            return Single.fromPublisher(repository.save(card));
        }).map(DebitCardMapper::toDto)
        .doOnSuccess(cardEventPublisher::cardSaved);
    }

    @Override
    public Completable delete(String cardNumber) {
        log.info("Deleting debit card {}", cardNumber);
        return findEntity(cardNumber)
                .flatMapCompletable(card -> Completable.fromPublisher(repository.delete(card))
                        .doOnComplete(() -> cardEventPublisher.cardDeleted(cardNumber)));
    }

    private List<String> distinct(List<String> accounts) {
        return new ArrayList<>(accounts.stream().distinct().toList());
    }

    private String newCardNumber() {
        return "DC" + UUID.randomUUID().toString().replace("-", "").substring(0, 14).toUpperCase();
    }

    private Single<DebitCard> findEntity(String cardNumber) {
        return Maybe.fromPublisher(repository.findByCardNumber(cardNumber))
                .switchIfEmpty(Single.error(new NotFoundException("Debit card not found: " + cardNumber)));
    }

    /** Checks, against the local copy, that every account exists and belongs to the holder. */
    private Single<List<String>> validateAccounts(String customerDocument, List<String> accounts) {
        return Single.fromPublisher(knownAccountRepository.findAllById(accounts).collectList())
                .flatMap(found -> {
                    if (found.size() != accounts.size()) {
                        return Single.<List<String>>error(new BusinessException(
                                "One or more accounts do not exist"));
                    }
                    boolean otherOwner = found.stream()
                            .anyMatch(a -> !customerDocument.equals(a.getCustomerDocument()));
                    if (otherOwner) {
                        return Single.<List<String>>error(new BusinessException(
                                "All accounts must belong to the card holder"));
                    }
                    return Single.just(accounts);
                });
    }
}