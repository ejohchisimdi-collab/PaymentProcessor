package com.chisimdi.PaymentProcessor.repository;

import com.chisimdi.PaymentProcessor.models.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PriceRepository extends JpaRepository<Price,Integer> {
}
