package com.bank.debitcard.service.impl;

import com.bank.debitcard.dto.DebitCardDto;
import com.bank.debitcard.exception.BusinessException;
import com.bank.debitcard.exception.NotFoundException;
import com.bank.debitcard.model.DebitCard;
import com.bank.debitcard.model.KnownAccount;
import com.bank.debitcard.repository.DebitCardRepository;
import com.bank.debitcard.repository.KnownAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for {@link DebitCardServiceImpl}. */
@ExtendWith(MockitoExtension.class)
class DebitCardServiceImplTest {

    @Mock
    private DebitCardRepository repository;

    @Mock
    private KnownAccountRepository knownAccountRepository;

    @InjectMocks
    private DebitCardServiceImpl service;

    private DebitCard card() {
        return DebitCard.builder().cardNumber("DC1").customerDocument("123")
                .accountNumbers(new java.util.ArrayList<>(List.of("A", "B")))
                .primaryAccount("A").build();
    }

    private void saveReturnsSameEntity() {
        when(repository.save(any(DebitCard.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));
    }

    @Test
    void createShouldDefaultPrimaryAccountToTheFirstOne() {
        when(knownAccountRepository.findAllById(List.of("A", "B")))
                .thenReturn(Flux.just(known("A", "123"), known("B", "123")));
        saveReturnsSameEntity();
        DebitCardDto dto = DebitCardDto.builder().customerDocument("123")
                .accountNumbers(List.of("A", "B", "A")).build();

        service.create(dto)
                .test()
                .assertValue(c -> "A".equals(c.getPrimaryAccount())
                        && c.getAccountNumbers().size() == 2
                        && c.getCardNumber().startsWith("DC"))
                .assertComplete();
    }

    @Test
    void createShouldFailWhenPrimaryAccountIsNotLinked() {
        DebitCardDto dto = DebitCardDto.builder().customerDocument("123")
                .accountNumbers(List.of("A")).primaryAccount("Z").build();

        service.create(dto).test().assertError(BusinessException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void findByCardNumberShouldFailWhenMissing() {
        when(repository.findByCardNumber("X")).thenReturn(Mono.empty());
        service.findByCardNumber("X").test().assertError(NotFoundException.class);
    }

    @Test
    void findAllAndFindByCustomerShouldReturnCards() {
        when(repository.findAll()).thenReturn(Flux.just(card(), card()));
        when(repository.findByCustomerDocument("123")).thenReturn(Flux.just(card()));

        service.findAll().test().assertValueCount(2).assertComplete();
        service.findByCustomer("123").test().assertValueCount(1).assertComplete();
    }

    @Test
    void changePrimaryAccountShouldWorkForALinkedAccount() {
        when(repository.findByCardNumber("DC1")).thenReturn(Mono.just(card()));
        saveReturnsSameEntity();

        service.changePrimaryAccount("DC1", "B")
                .test()
                .assertValue(c -> "B".equals(c.getPrimaryAccount()))
                .assertComplete();
    }

    @Test
    void changePrimaryAccountShouldFailForAnUnlinkedAccount() {
        when(repository.findByCardNumber("DC1")).thenReturn(Mono.just(card()));

        service.changePrimaryAccount("DC1", "Z").test().assertError(BusinessException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void updateShouldMovePrimaryToFirstAccountWhenOldOneIsUnlinked() {
        when(repository.findByCardNumber("DC1")).thenReturn(Mono.just(card()));
        when(knownAccountRepository.findAllById(List.of("B", "C")))
                .thenReturn(Flux.just(known("B", "123"), known("C", "123")));
        saveReturnsSameEntity();
        DebitCardDto dto = DebitCardDto.builder().customerDocument("123")
                .accountNumbers(List.of("B", "C")).build();

        service.update("DC1", dto)
                .test()
                .assertValue(c -> "B".equals(c.getPrimaryAccount()))
                .assertComplete();
    }

    @Test
    void deleteShouldRemoveTheCard() {
        DebitCard c = card();
        when(repository.findByCardNumber("DC1")).thenReturn(Mono.just(c));
        when(repository.delete(c)).thenReturn(Mono.empty());

        service.delete("DC1").test().assertComplete();
        verify(repository).delete(c);
    }

    @Test
    void createShouldFailWhenAnAccountIsUnknown() {
        when(knownAccountRepository.findAllById(List.of("A", "B")))
                .thenReturn(Flux.just(known("A", "123")));
        DebitCardDto dto = DebitCardDto.builder().customerDocument("123")
                .accountNumbers(List.of("A", "B")).build();

        service.create(dto).test().assertError(BusinessException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void createShouldFailWhenAnAccountBelongsToAnotherCustomer() {
        when(knownAccountRepository.findAllById(List.of("A")))
                .thenReturn(Flux.just(known("A", "999")));
        DebitCardDto dto = DebitCardDto.builder().customerDocument("123")
                .accountNumbers(List.of("A")).build();

        service.create(dto).test().assertError(BusinessException.class);
        verify(repository, never()).save(any());
    }

    private KnownAccount known(String number, String customer) {
        return KnownAccount.builder().accountNumber(number).customerDocument(customer)
                .accountType("SAVINGS").build();
    }
}