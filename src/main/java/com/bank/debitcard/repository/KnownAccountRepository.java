package com.bank.debitcard.repository;

import com.bank.debitcard.model.KnownAccount;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/** Local copy of accounts. */
public interface KnownAccountRepository extends ReactiveMongoRepository<KnownAccount, String> {
}