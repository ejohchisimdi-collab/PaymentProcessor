package com.chisimdi.PaymentProcessor.controllers;

import com.chisimdi.PaymentProcessor.models.MerchantAccount;
import com.chisimdi.PaymentProcessor.models.MerchantAccountDTO;
import com.chisimdi.PaymentProcessor.models.MerchantSetting;
import com.chisimdi.PaymentProcessor.models.MerchantSettingDTO;
import com.chisimdi.PaymentProcessor.services.MerchantService;
import com.chisimdi.PaymentProcessor.utils.NewMerchantSettingUtil;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/merchants")
public class MerchantController {
    private MerchantService merchantService;

    public MerchantController(MerchantService merchantService){
        this.merchantService=merchantService;
    }
@Operation(summary = "Creates merchant setting",description = "Creates a merchant setting for a merchant. One setting per merchant. Available only to merchants")
    @PreAuthorize("hasRole('ROLE_Merchant') and principal.userId == #merchantId")
    @PostMapping("/settings/{merchantId}")
    public MerchantSettingDTO createMerchantSettings(@PathVariable("merchantId")int merchantId, @Valid @RequestBody MerchantSetting merchantSetting){
        return merchantService.createMerchantSetting(merchantId,merchantSetting);
    }
    @Operation(summary = "Updates Merchant Setting",description = "Updates an already existing setting for a merchant, acessible only to merchants")
    @PreAuthorize("hasRole('ROLE_Merchant') and principal.userId == #merchantId")
    @PutMapping("/settings/{merchantId}")
    public MerchantSettingDTO updateMerchantSettings(@PathVariable("merchantId")int merchantId, @Valid @RequestBody NewMerchantSettingUtil merchantSettingUtil){
        return merchantService.updateMerchantSetting(merchantId,merchantSettingUtil);
    }

    @Operation(summary = "Gets setting by merchant",description = "Retrieves the single setting belonging to a merchant, accessible only to merchants")
    @PreAuthorize("hasRole('ROLE_Admin') or principal.userId == #merchantId")
    @GetMapping("/settings/{merchantId}")
    public MerchantSettingDTO findMerchantSettingsByMerchantId(@PathVariable("merchantId")int merchantId){
        return merchantService.findMerchantSettingByMerchantId(merchantId);
    }
    @Operation(summary = "Creates a merchant account",description = "Crates a single merchant account for a merchant. Accessible only to merchants")
    @PreAuthorize("hasRole('ROLE_Merchant') and principal.userId == #merchantId")
    @PostMapping("/accounts/{merchantId}")
    public MerchantAccountDTO createMerchantAccount(@PathVariable("merchantId")int merchantId, @Valid @RequestBody MerchantAccount merchantAccount){
        return merchantService.createMerchantAccount(merchantId,merchantAccount);
    }

    @Operation(summary = "Locates merchant account",description = "Retrieves the single merchant account belonging to a merchant. Available only to merchants and admins")
    @PreAuthorize("hasRole('ROLE_Admin') or principal.userId == #merchantId")
    @GetMapping("/accounts/{merchantId}")
    public MerchantAccountDTO findAccountByMerchant(@PathVariable("merchantId")int merchantId){
        return merchantService.findMerchantAccountByMerchantId(merchantId);
    }
    @Operation(summary = "Retrieves all Merchant accounts",description = "Locates accounts belonging to all merchants. Accessible only to admins")
    @PreAuthorize("hasRole('ROLE_Admin')")
    @GetMapping("/accounts")
    public List<MerchantAccountDTO>findAllMerchantAccounts(@RequestParam(defaultValue = "0")int pageNumber,@RequestParam(defaultValue = "10")int size){
        return merchantService.findAllMerchantAccounts(pageNumber, size);
    }
}
