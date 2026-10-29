package com.bank.debitcard.repository;

import com.bank.debitcard.model.DebitPayment;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

/** Reactive repository for debit payments. */
public interface DebitPaymentRepository extends ReactiveMongoRepository<DebitPayment, String> {
}