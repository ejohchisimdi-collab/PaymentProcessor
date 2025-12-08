package com.chisimdi.PaymentProcessor.controllers;

import com.chisimdi.PaymentProcessor.models.PaymentDTO;
import com.chisimdi.PaymentProcessor.services.PaymentService;
import com.chisimdi.PaymentProcessor.utils.PayUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private PaymentService paymentService;

    public PaymentController(PaymentService paymentService){
        this.paymentService=paymentService;
    }

    @Operation(summary = "creates a payment",description = "Creates a payment. Guaranteed settlements for credit cards but may be reversed later by bank accounts." +
            " Available only to merchants")
    @PreAuthorize("hasRole('ROLE_Merchant') and principal.userId == #payUtil.merchantId")
    @PostMapping("/pay")
    public PaymentDTO pay(@RequestHeader("Idempotency-key")String idempotencyKey, @RequestBody @Valid PayUtil payUtil){
        return paymentService.payWithRetries(idempotencyKey,payUtil.getMerchantId(), payUtil.getAccountId(), payUtil.getAmount(),payUtil.getLocation());
    }
    @Operation(summary = "Locates all payments",description = "Retrieves all payments, Available only to merchants")
    @PreAuthorize("hasRole('ROLE_Admin')")
    @GetMapping("/")
    public List<PaymentDTO>findAllPayments(@RequestParam(defaultValue = "0")int pageNumber, @RequestParam(defaultValue = "10")int size){
        return paymentService.findAllPayments(pageNumber, size);
    }

    @Operation(summary = "Finds  payments by merchants",description = "locates all payments made by a merchant. Available to admins and merchants")
    @PreAuthorize("hasRole('ROLE_Admin') or principal.userId == #merchantId")
    @GetMapping("/{merchantId}")
    public List<PaymentDTO>findSpecificPayments(@PathVariable("merchantId")int merchantId,@RequestParam(defaultValue = "0")int pageNumber,@RequestParam(defaultValue = "10")int size){
        return paymentService.findByMerchantId(merchantId, pageNumber, size);
    }
}
