package com.chisimdi.PaymentProcessor.utils;

import jakarta.persistence.Id;
import jakarta.validation.constraints.Positive;

public class MakeASubscriptionUtil {
    @Positive
    private int accountId;
    @Positive
    private int customerId;
    @Positive
    private int priceId;

    public void setPriceId(int priceId) {
        this.priceId = priceId;
    }

    public int getPriceId() {
        return priceId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getCustomerId() {
        return customerId;
    }

}
