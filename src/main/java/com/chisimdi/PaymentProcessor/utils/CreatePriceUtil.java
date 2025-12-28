package com.chisimdi.PaymentProcessor.utils;

import com.chisimdi.PaymentProcessor.models.PaymentInterval;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class CreatePriceUtil {
    @NotNull
    private BigDecimal amount;
    @NotNull
    private String productName;
    @Positive
    private int intervalCount;
    @NotNull
    private PaymentInterval paymentInterval;
    @Positive
    private int retryAttempts;
    private int merchantId;

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    public void setPaymentInterval(PaymentInterval paymentInterval) {
        this.paymentInterval = paymentInterval;
    }

    public PaymentInterval getPaymentInterval() {
        return paymentInterval;
    }

    public int getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getIntervalCount() {
        return intervalCount;
    }

    public void setIntervalCount(int intervalCount) {
        this.intervalCount = intervalCount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

}
