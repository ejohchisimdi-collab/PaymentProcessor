package com.chisimdi.PaymentProcessor.repository;

import com.chisimdi.PaymentProcessor.models.PaymentIdempotency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentIdempotencyRepository extends JpaRepository<PaymentIdempotency,Integer> {

    public PaymentIdempotency findByIdempotencyKey(String idempotencyKey);
}
