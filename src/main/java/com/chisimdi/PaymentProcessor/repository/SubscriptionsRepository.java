package com.chisimdi.PaymentProcessor.repository;

import com.chisimdi.PaymentProcessor.models.SubscriptionStatus;
import com.chisimdi.PaymentProcessor.models.Subscriptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionsRepository extends JpaRepository<Subscriptions,Integer> {
    List<Subscriptions>findBySubscriptionStatus(SubscriptionStatus subscriptionStatus);

}
