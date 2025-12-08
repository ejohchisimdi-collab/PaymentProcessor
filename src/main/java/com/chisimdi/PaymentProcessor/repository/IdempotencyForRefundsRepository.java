package com.chisimdi.PaymentProcessor.repository;

import com.chisimdi.PaymentProcessor.models.IdempotencyForRefunds;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotencyForRefundsRepository extends JpaRepository<IdempotencyForRefunds,Integer> {
IdempotencyForRefunds findByIdempotencyKey(String idempotencyKey);
}
