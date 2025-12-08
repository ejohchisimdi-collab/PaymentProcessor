package com.chisimdi.PaymentProcessor.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PaymentDTO {
   private int id;
   private int accountId;
    private int merchantId;
    private BigDecimal amount;
   private AccountType accountType;
    private PaymentStatus status;
    private Location location;
    private List<String> warnings =new ArrayList<>();
    private LocalDateTime localDate=LocalDateTime.now();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public int getMerchantId() {
        return merchantId;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public LocalDateTime getLocalDate() {
        return localDate;
    }

    public void setLocalDate(LocalDateTime localDate) {
        this.localDate = localDate;
    }
}

