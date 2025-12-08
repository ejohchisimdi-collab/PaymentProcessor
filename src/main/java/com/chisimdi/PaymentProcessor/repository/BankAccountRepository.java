package com.chisimdi.PaymentProcessor.repository;

import com.chisimdi.PaymentProcessor.models.BankAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount,Integer> {
    Page<BankAccount>findByCustomerId(int customerId, Pageable pageable);
}
