package com.bank.debitcard.event;

import com.bank.debitcard.model.KnownAccount;
import com.bank.debitcard.repository.KnownAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Unit tests for {@link AccountEventHandler}. */
@ExtendWith(MockitoExtension.class)
class AccountEventHandlerTest {

    @Mock
    private KnownAccountRepository repository;

    @InjectMocks
    private AccountEventHandler handler;

    @Test
    void createdEventShouldStoreTheAccount() {
        when(repository.save(any(KnownAccount.class))).thenAnswer(i -> Mono.just(i.getArgument(0)));

        handler.handle("CREATED|ACC1|123|SAVINGS").block();

        verify(repository).save(argThat(a -> "ACC1".equals(a.getAccountNumber())
                && "123".equals(a.getCustomerDocument()) && "SAVINGS".equals(a.getAccountType())));
    }

    @Test
    void deletedEventShouldRemoveTheAccount() {
        when(repository.deleteById("ACC1")).thenReturn(Mono.empty());

        handler.handle("DELETED|ACC1").block();

        verify(repository).deleteById("ACC1");
    }

    @Test
    void unknownEventShouldBeIgnored() {
        handler.handle("WHATEVER|X").block();

        verify(repository, never()).save(any());
        verify(repository, never()).deleteById(any(String.class));
    }
}