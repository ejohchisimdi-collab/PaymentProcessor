package com.chisimdi.PaymentProcessor.controllers;

import com.chisimdi.PaymentProcessor.models.SubscriptionsDTO;
import com.chisimdi.PaymentProcessor.services.SubscriptionsService;
import com.chisimdi.PaymentProcessor.utils.MakeASubscriptionUtil;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RequestMapping("/subscriptions")
@RestController
public class SubscriptionsController {
    private SubscriptionsService subscriptionsService;

    public SubscriptionsController(SubscriptionsService subscriptionsService){
        this.subscriptionsService=subscriptionsService;
    }

    @PreAuthorize(" hasRole('ROLE_Customer') and principal.userId == #subscriptionUtil.customerId")
    @PostMapping("/")
    public SubscriptionsDTO makeASubscription(@Valid @RequestBody MakeASubscriptionUtil subscriptionUtil){
        return subscriptionsService.makeASubscription(subscriptionUtil.getAccountId(), subscriptionUtil.getCustomerId(), subscriptionUtil.getPriceId());
    }
}
