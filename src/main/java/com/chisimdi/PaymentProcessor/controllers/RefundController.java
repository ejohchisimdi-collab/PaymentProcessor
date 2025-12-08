package com.chisimdi.PaymentProcessor.controllers;

import com.chisimdi.PaymentProcessor.models.Refund;
import com.chisimdi.PaymentProcessor.models.RefundDTO;
import com.chisimdi.PaymentProcessor.services.RefundService;
import com.chisimdi.PaymentProcessor.utils.RefundUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/refunds")
public class RefundController {
    private RefundService refundService;

    public RefundController(RefundService refundService){
        this.refundService=refundService;
    }
   @Operation(summary = "Crates a refund",description = "Creates a refund. Available only to merchants ")
    @PreAuthorize("hasRole('ROLE_Merchant') and principal.userId == #refundUtil.merchantId")
    @PostMapping("/")
    public RefundDTO makeARefund(@RequestHeader("Idempotency-Key")String idempotencyKey, @Valid @RequestBody RefundUtil refundUtil){
        return refundService.makeARefundWithRetries(idempotencyKey,refundUtil.getMerchantId(), refundUtil.getPaymentId(), refundUtil.getAmount());
    }

    @Operation(summary = "finds refund by merchant",description = "retrieves all refunds belonging to a merchant. Accessible only to admins ")
    @PreAuthorize("hasRole('ROLE_Admin') or principal.userId == #merchantId")
    @GetMapping("/{merchantId}")
    public List<RefundDTO>findRefundsByMerchant(@PathVariable("merchantId")int merchantId,@RequestParam(defaultValue = "0")int pageNumber,@RequestParam(defaultValue = "10")int size){
        return refundService.findAllByMerchants(merchantId,pageNumber,size);
    }

    @Operation(summary = "Finds all refunds",description = "Retrieves all  refunds, available only to merchants")
    @PreAuthorize("hasRole('ROLE_Admin')")
    @GetMapping("/")
    public List<RefundDTO>findAllRefunds(@RequestParam(defaultValue = "0")int pageNumber,@RequestParam(defaultValue = "10")int size){
        return refundService.findAllPayments(pageNumber, size);
    }
}
