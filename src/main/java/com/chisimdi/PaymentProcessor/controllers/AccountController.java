package com.chisimdi.PaymentProcessor.controllers;

import com.chisimdi.PaymentProcessor.models.BankAccount;
import com.chisimdi.PaymentProcessor.models.BankAccountDTO;
import com.chisimdi.PaymentProcessor.models.CreditCard;
import com.chisimdi.PaymentProcessor.models.CreditCardDTO;
import com.chisimdi.PaymentProcessor.services.CustomerAccountService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    private CustomerAccountService customerAccountService;

    public AccountController(CustomerAccountService customerAccountService){
        this.customerAccountService=customerAccountService;
    }
    @Operation(description = "Create credit card",summary = "Creates a credit card available only to customers. Must belong to the users Id ")
    @PreAuthorize("hasRole('ROLE_Customer') and principal.userId == #userId")
    @PostMapping("/credit-card/{userId}")
    public CreditCardDTO createCreditCard(@PathVariable("userId")int userId, @Valid @RequestBody CreditCard creditCard){
        return customerAccountService.createNewCreditCard(userId,creditCard);
    }
    @Operation(description = "Create a bank account",summary = "Creates a bank account available only to customers. Must belong to the users Id ")
@PreAuthorize("hasRole('ROLE_Customer') and principal.userId == #userId")
    @PostMapping("/bank-account/{userId}")
    public BankAccountDTO createBankAccount(@PathVariable("userId")int userId, @Valid @RequestBody BankAccount bankAccount){
        return customerAccountService.createNewBankAccount(userId,bankAccount);
    }
    @Operation(description = "View all bank Accounts",summary = "Retrieves all bank accounts, available only to admins")
    @PreAuthorize("hasRole('ROLE_Admin')")
    @GetMapping("/bank-accounts")
    public List<BankAccountDTO>findAllBankAccounts(@RequestParam(defaultValue = "0")int pageNumber ,@Valid @RequestParam(defaultValue = "10")int size){
        return customerAccountService.findAllBankAccounts(pageNumber,size);
    }
    @Operation(description = "view all credit cards",summary = "Retrieves all credit cards, available only to admins")
@PreAuthorize("hasRole('ROLE_Admin')")
    @GetMapping("/credit-cards")
    public List<CreditCardDTO>findAllCreditCard(@RequestParam(defaultValue = "0")int pageNumber,@RequestParam(defaultValue = "10")int size){
        return customerAccountService.findAllCreditCards(pageNumber, size);
    }
    @Operation(description = "View all bank Accounts by user",summary = "Retrieves all bank accounts, available only to admins or customers but only those belonging to their id")
    @PreAuthorize("hasRole('ROLE_Admin') or principal.userId == #userId")
    @GetMapping("/bank-accounts/{userId}")
    public List<BankAccountDTO>findAllBankAccountsByUser(@RequestParam(defaultValue = "0")int pageNumber ,@RequestParam(defaultValue = "10")int size,@PathVariable("userId")int userId){
        return customerAccountService.findBankAccountByUsers(pageNumber, size, userId);
    }
    @Operation(description = "View all credit cards by user",summary = "Retrieves all credit cards, available only to admins or to users but only those belonging to their userId")
    @PreAuthorize("hasRole('ROLE_Admin') or principal.userId == #userId")
    @GetMapping("/credit-cards/{userId}")
    public List<CreditCardDTO>findAllCreditCardByUser(@RequestParam(defaultValue = "0")int pageNumber,@RequestParam(defaultValue = "10")int size,@PathVariable("userId")int userId){
        return customerAccountService.findCreditCardsByUsers(pageNumber, size, userId);
    }




}
