package com.chisimdi.PaymentProcessor.repository;

import com.chisimdi.PaymentProcessor.models.MerchantAccount;
import com.chisimdi.PaymentProcessor.models.MerchantSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantAccountRepository extends JpaRepository<MerchantAccount,Integer> {
MerchantAccount findByMerchantId(int merchantId);
}
