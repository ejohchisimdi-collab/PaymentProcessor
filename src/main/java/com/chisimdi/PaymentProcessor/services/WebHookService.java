package com.chisimdi.PaymentProcessor.services;

import com.chisimdi.PaymentProcessor.models.MerchantSetting;
import com.chisimdi.PaymentProcessor.models.WebhookEvents;

import com.chisimdi.PaymentProcessor.models.WebhookRetries;
import com.chisimdi.PaymentProcessor.repository.MerchantSettingRepository;
import com.chisimdi.PaymentProcessor.repository.WebHookRetriesRepository;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class WebHookService {
    private static final Logger log = LoggerFactory.getLogger(WebHookService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    private WebHookRetriesRepository webHookRetriesRepository;
    private MerchantSettingRepository merchantSettingRepository;
    private JwtUtilService jwtUtilService;

    public WebHookService(WebHookRetriesRepository webHookRetriesRepository, MerchantSettingRepository merchantSettingRepository, JwtUtilService jwtUtilService) {
        this.webHookRetriesRepository = webHookRetriesRepository;
        this.merchantSettingRepository = merchantSettingRepository;
        this.jwtUtilService = jwtUtilService;
    }

    public void sendWebhook(String url, WebhookEvents webhookEvents) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setBearerAuth(jwtUtilService.generateToken("Machine", 1, "Webhook"));
        HttpEntity<WebhookEvents> entity = new HttpEntity<>(webhookEvents, httpHeaders);
        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);
    }


    public void retryWebhooks() {
        List<WebhookRetries> webhookRetries = webHookRetriesRepository.findByFailedAndDone(false, false);
        log.info("Retrying webhooks");
        for (int x = 0; x < webhookRetries.size(); x++) {
            WebhookRetries webhookRetries1 = webhookRetries.get(x);
            if (webhookRetries1.getMaxRetries() == webhookRetries1.getRetryCount()) {
              log.warn("Max retries reached for webhook");
                webhookRetries1.setFailed(true);
                webHookRetriesRepository.save(webhookRetries1);
                continue;
            }
            if (LocalDateTime.now().isBefore(webhookRetries1.getNextRetry())) {

                continue;
            }
            MerchantSetting merchantSetting = merchantSettingRepository.findByMerchantId(webhookRetries1.getMerchant().getId());
            WebhookEvents webhookEvents = new WebhookEvents();
            webhookEvents.setEventType(webhookRetries1.getEventType());
            webhookEvents.setMerchant(webhookRetries1.getMerchant());
            webhookEvents.setAmount(webhookRetries1.getAmount());
            webhookEvents.setAccountType(webhookRetries1.getAccountType());
            webhookEvents.setPaymentStatus(webhookRetries1.getPaymentStatus());
            webhookEvents.setRefundStatus(webhookRetries1.getRefundStatus());
            try {
                log.debug("Sending webhook");
                sendWebhook(merchantSetting.getMerchantEndpoint(), webhookEvents);
                webhookRetries1.setDone(true);
                webHookRetriesRepository.save(webhookRetries1);
            } catch (ResourceAccessException e) {
                log.warn("webhook sending failed update webhook ");
                webhookRetries1.setNextRetry(LocalDateTime.now().plusSeconds(((int) (5 * (Math.pow(2, webhookRetries1.getRetryCount()))))));
                webhookRetries1.setRetryCount(webhookRetries1.getRetryCount() + 1);
                webHookRetriesRepository.save(webhookRetries1);
            }


        }


    }
@Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void retrySendingWebhooks() {
        int retries = 0;
        while (retries < 5) {
            try {
                retryWebhooks();
            } catch (OptimisticLockException e) {
                retries++;
                if(retries==5)throw e;
            }


        }
    }
}