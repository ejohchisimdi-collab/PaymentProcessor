package com.chisimdi.PaymentProcessor.repository;

import com.chisimdi.PaymentProcessor.models.Refund;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefundRepository extends JpaRepository<Refund,Integer> {
    Page<Refund>findByPaymentMerchantId(int merchantId, Pageable pageable);
}
