package com.chisimdi.PaymentProcessor.repository;

import com.chisimdi.PaymentProcessor.models.WebhookEvents;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.nio.file.WatchEvent;

@Repository
public interface WebHookEventsRepository extends JpaRepository<WebhookEvents,Integer> {
    Page<WebhookEvents> findByMerchantId(int merchantId, Pageable pageable);
}
