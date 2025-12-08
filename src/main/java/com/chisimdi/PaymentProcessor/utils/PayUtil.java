package com.chisimdi.PaymentProcessor.utils;

import com.chisimdi.PaymentProcessor.models.Location;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class PayUtil {
    @NotNull
            @Positive
    int merchantId;
    @NotNull
            @Positive
    int accountId;
    @NotNull
            @Positive
    BigDecimal amount;
    @NotNull
            @Enumerated(EnumType.STRING)
    Location location;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public int getMerchantId() {
        return merchantId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getAccountId() {
        return accountId;
    }

}
