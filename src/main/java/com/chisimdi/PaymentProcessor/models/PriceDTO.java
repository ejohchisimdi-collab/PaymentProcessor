package com.chisimdi.PaymentProcessor.models;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;

import java.math.BigDecimal;

public class PriceDTO {
    private int id;
    private BigDecimal amount;
    private int merchantId;
    private Interval interval;
    private int intervalCount;
    @Column(unique = true)
    private String productName;

    public void setIntervalCount(int intervalCount) {
        this.intervalCount = intervalCount;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }

    public Interval getInterval() {
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

}
