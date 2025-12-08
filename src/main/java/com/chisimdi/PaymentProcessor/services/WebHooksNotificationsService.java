package com.chisimdi.PaymentProcessor.services;

import com.chisimdi.PaymentProcessor.models.*;
import com.chisimdi.PaymentProcessor.repository.WebHookEventsRepository;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class WebHooksNotificationsService {
    private WebHookEventsRepository webHookEventsRepository;

    public WebHooksNotificationsService(WebHookEventsRepository webHookEventsRepository){
        this.webHookEventsRepository=webHookEventsRepository;
    }
public WebHookEventDTO toWebhookEventDTO(WebhookEvents webhookEvents){
        WebHookEventDTO webHookEventDTO=new WebHookEventDTO();
        if(webhookEvents.getEventType()!=null){
            webHookEventDTO.setEventType(webhookEvents.getEventType());
        }
        if(webhookEvents.getAccountType()!=null){
            webHookEventDTO.setAccountType(webhookEvents.getAccountType());
        }
        if(webhookEvents.getPaymentStatus()!=null){
            webHookEventDTO.setPaymentStatus(webhookEvents.getPaymentStatus());
        }
        if(webhookEvents.getRefundStatus()!=null){
            webHookEventDTO.setRefundStatus(webhookEvents.getRefundStatus());
        }
        if(webhookEvents.getAmount()!=null){
            webHookEventDTO.setAmount(webhookEvents.getAmount());
        }
        if ((webhookEvents.getWarnings()!=null)){
            webHookEventDTO.setWarnings(webhookEvents.getWarnings());
        }
           webHookEventDTO.setId(webhookEvents.getId());


return webHookEventDTO; }
    public void createNotifications(WebhookEvents webhookEvents){
        webHookEventsRepository.save(webhookEvents);
    }
   public List<WebHookEventDTO>viewAllWebhookEvents(int pageNumber,int size){
        Page<WebhookEvents>webhookEvents=webHookEventsRepository.findAll(PageRequest.of(pageNumber, size));
    List<WebHookEventDTO>webHookEventDTOS=new ArrayList<>();
    for(WebhookEvents w: webhookEvents){
        webHookEventDTOS.add(toWebhookEventDTO(w));
    }
    return webHookEventDTOS;
    }
    public List<WebHookEventDTO>viewWebhooksByMerchant(int merchantId, int pageNumber, int size ){
      Page<WebhookEvents>webhookEvents=   webHookEventsRepository.findByMerchantId(merchantId,PageRequest.of(pageNumber,size));
        List<WebHookEventDTO>webHookEventDTOS=new ArrayList<>();
        for(WebhookEvents w: webhookEvents){
            webHookEventDTOS.add(toWebhookEventDTO(w));
        }
        return webHookEventDTOS;
    }
}
