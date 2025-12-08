package com.chisimdi.PaymentProcessor.repository;

import com.chisimdi.PaymentProcessor.models.CreditCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard,Integer> {
    Page<CreditCard>findByCustomerId(int customerId, Pageable pageable);
}
