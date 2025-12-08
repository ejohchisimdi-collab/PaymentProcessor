package com.chisimdi.PaymentProcessor.repository;

import com.chisimdi.PaymentProcessor.models.MerchantSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MerchantSettingRepository extends JpaRepository<MerchantSetting,Integer> {
    MerchantSetting findByMerchantId(int merchantId);
}
