package com.chisimdi.PaymentProcessor.repository;

import com.chisimdi.PaymentProcessor.models.WebhookRetries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WebHookRetriesRepository extends JpaRepository<WebhookRetries,Integer> {
    List<WebhookRetries>findByFailedAndDone(Boolean failed,Boolean done);
}
