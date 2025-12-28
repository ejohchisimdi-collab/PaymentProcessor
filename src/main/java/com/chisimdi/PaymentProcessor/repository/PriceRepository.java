package com.chisimdi.PaymentProcessor.repository;

import com.chisimdi.PaymentProcessor.models.Prices;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceRepository extends JpaRepository<Prices,Integer> {
    Page<Prices>findByMerchantId(int merchantId, Pageable pageable);
}
