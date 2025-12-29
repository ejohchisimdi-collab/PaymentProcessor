package com.chisimdi.PaymentProcessor.controllers;

import com.chisimdi.PaymentProcessor.models.SubscriptionsDTO;
import com.chisimdi.PaymentProcessor.services.SubscriptionsService;
import com.chisimdi.PaymentProcessor.utils.MakeASubscriptionUtil;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @PreAuthorize("hasRole('ROLE_Admin') or principal.userId == #customerId")
    @GetMapping("/customers/{customerId}")
    public List<SubscriptionsDTO>findAllSubscriptionsByCustomer(@PathVariable("customerId")int customerId,@RequestParam(defaultValue = "0")int pageNumber,@RequestParam(defaultValue = "10")int size){
        return subscriptionsService.findSubscriptionsByCustomer(customerId, pageNumber, size);
    }

    @PreAuthorize("hasRole('ROLE_Admin') or principal.userId == #merchantId")
    @GetMapping("/merchants/{merchantId}")
    public List<SubscriptionsDTO>findAllSubscriptionsByMerchant(@PathVariable("merchantId")int merchantId,@RequestParam(defaultValue = "0")int pageNumber,@RequestParam(defaultValue = "10")int size){
        return subscriptionsService.findByMerchant(merchantId, pageNumber, size);
    }

    @PreAuthorize("hasRole('ROLE_Admin')")
    public List<SubscriptionsDTO>findAllSubscriptions(@RequestParam(defaultValue = "0")int pageNumber,@RequestParam(defaultValue = "10")int size){
        return subscriptionsService.findAllSubscriptions( pageNumber, size);
    }

    @PreAuthorize("hasRole('ROLE_Customer) and principal.userId == #customerId")
    @PostMapping("/cancellation")
    public SubscriptionsDTO cancelSubscription(@RequestParam int customerId,@RequestParam int subscriptionId){
        return subscriptionsService.cancelSubscription(customerId,subscriptionId);
    }


}
