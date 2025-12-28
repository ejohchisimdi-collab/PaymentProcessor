package com.chisimdi.PaymentProcessor.models;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
public class Prices {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(precision = 16,scale = 2)
    private BigDecimal amount;
    @ManyToOne
    private User merchant;
    @Enumerated(EnumType.STRING)
    private PaymentInterval paymentInterval;
    private int intervalCount;
    private String productName;
    private int retryAttempts;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    public void setMerchant(User merchant) {
        this.merchant = merchant;
    }

    public User getMerchant() {
        return merchant;
    }

    public int getIntervalCount() {
        return intervalCount;
    }

    public PaymentInterval getPaymentInterval() {
        return paymentInterval;
    }

    public void setPaymentInterval(PaymentInterval paymentInterval) {
        this.paymentInterval = paymentInterval;
    }

    public void setIntervalCount(int intervalCount) {
        this.intervalCount = intervalCount;
    }

    public int getRetryAttempts() {
        return retryAttempts;
    }

    public void setRetryAttempts(int retryAttempts) {
        this.retryAttempts = retryAttempts;
    }
}
