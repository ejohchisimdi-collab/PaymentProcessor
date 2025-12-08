package com.chisimdi.PaymentProcessor.controllers;

import com.chisimdi.PaymentProcessor.models.WebHookEventDTO;
import com.chisimdi.PaymentProcessor.models.WebhookEvents;
import com.chisimdi.PaymentProcessor.services.WebHooksNotificationsService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class WebhookNotificationController {
    private WebHooksNotificationsService notificationsService;

    public WebhookNotificationController(WebHooksNotificationsService notificationsService){
        this.notificationsService=notificationsService;

    }
    @Operation(summary = "Mock merchant webhook endpoint")
@PreAuthorize("hasRole('ROLE_Webhook')")
    @PostMapping( "/webhooks/notifications")
    public void createNotification(@RequestBody WebhookEvents webhookEvents){
        notificationsService.createNotifications(webhookEvents);
    }
    @Operation(summary = "View all webhooks",description = "retrieves all webhooks stored from the mock endpoint, available only to admins")
    @PreAuthorize("hasRole('ROLE_Admin')")
    @GetMapping("/webhooks")
    public List<WebHookEventDTO>findAllWebhookEvents(@RequestParam(defaultValue = "0")int pageNumber, @RequestParam(defaultValue = "10")int size){
        return notificationsService.viewAllWebhookEvents(pageNumber, size);

    }
    @Operation(summary = "View all webhooks by merchant",description = "Retrieves all webhooks belonging to a merchant. Available only to merchants and admins")
    @PreAuthorize("hasRole('ROLE_Admin') or principal.userId == #merchantId")
    @GetMapping("/webhooks/{merchantId}")
    public List<WebHookEventDTO>findAllByMerchants(@PathVariable("merchantId")int merchantId,@RequestParam(defaultValue = "0")int pageNumber,@RequestParam(defaultValue = "10")int size){
        return notificationsService.viewWebhooksByMerchant(merchantId, pageNumber, size);
    }
}
