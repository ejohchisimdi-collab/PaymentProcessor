package com.chisimdi.PaymentProcessor.models;

import jakarta.persistence.Column;

import java.math.BigDecimal;

public class PriceDTO {
    private int id;
    private BigDecimal amount;
    private int merchantId;
    private PaymentInterval interval;
    private int intervalCount;
    @Column(unique = true)
    private String productName;
    private int retryAttempts;

    public void setIntervalCount(int intervalCount) {
        this.intervalCount = intervalCount;
    }

    public void setInterval(PaymentInterval interval) {
        this.interval = interval;
    }

    public PaymentInterval getInterval() {
        return interval;
    }

    public int getIntervalCount() {
        return intervalCount;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductName() {
        return productName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public int getMerchantId() {
        return merchantId;
    }

    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

}
