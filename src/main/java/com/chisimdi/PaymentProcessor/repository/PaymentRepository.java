package com.chisimdi.PaymentProcessor.repository;

import com.chisimdi.PaymentProcessor.models.AccountType;
import com.chisimdi.PaymentProcessor.models.Payment;
import com.chisimdi.PaymentProcessor.models.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment,Integer> {
    List<Payment> findByAccountCustomerIdAndPaymentStatus(int id, PaymentStatus status);
    List<Payment>findByAccountId(int accountId);
    List<Payment>findByAccountType(AccountType accountType);
    Page<Payment> findByMerchantId(int merchantId, Pageable pageable);
}
