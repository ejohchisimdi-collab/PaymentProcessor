package com.chisimdi.PaymentProcessor.controllers;

import com.chisimdi.PaymentProcessor.models.PriceDTO;
import com.chisimdi.PaymentProcessor.services.PriceService;
import com.chisimdi.PaymentProcessor.utils.CreatePriceUtil;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/prices")
@RestController
public class PricesController {
    private PriceService priceService;

    public PricesController(PriceService priceService){
        this.priceService=priceService;
    }
    @PreAuthorize("hasRole('ROLE_Merchant') and principal.userId == #priceUtil.merchantId")
    @PostMapping("/")
    public PriceDTO createPrice(@Valid @RequestBody CreatePriceUtil priceUtil){
        return priceService.createPrice(priceUtil.getAmount(),priceUtil.getProductName(),priceUtil.getIntervalCount(),priceUtil.getPaymentInterval(), priceUtil.getRetryAttempts(), priceUtil.getMerchantId());

    }

    @GetMapping("/{merchantId}")
    public List<PriceDTO>findAllPricesByMerchant(@PathVariable("merchantId")int merchantId,@RequestParam(defaultValue = "0")int pageNumber,@RequestParam(defaultValue = "10")int size){
        return priceService.findPricesByMerchantId(merchantId, pageNumber, size);
    }

    @GetMapping("/")
    public List<PriceDTO>findAllPrices(@RequestParam(defaultValue = "0")int pageNumber,@RequestParam(defaultValue = "10")int size){
        return priceService.findAllPrices(pageNumber, size);
    }


}
