package com.chisimdi.PaymentProcessor.repository;

import com.chisimdi.PaymentProcessor.models.SubscriptionStatus;
import com.chisimdi.PaymentProcessor.models.Subscriptions;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionsRepository extends JpaRepository<Subscriptions,Integer> {
    List<Subscriptions>findBySubscriptionStatus(SubscriptionStatus subscriptionStatus);
    Page<Subscriptions>findByCustomersId(int customerId, Pageable pageable);
    Page<Subscriptions>findByPricesMerchantId(int merchantId,Pageable pageable);
    Optional<Subscriptions> findByIdAndCustomersId(int id, int customersId);

}
