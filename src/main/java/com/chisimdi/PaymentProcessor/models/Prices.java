package com.chisimdi.PaymentProcessor.models;

import jakarta.persistence.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import javax.annotation.processing.Generated;
import java.math.BigDecimal;

@Entity
public class Price {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(precision = 16,scale = 2)
    private BigDecimal amount;
    @ManyToOne
    private User merchant;
    @Enumerated(EnumType.STRING)
    private Interval interval;
    private int intervalCount;
    @Column(unique = true)
    private String productName;

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

    public Interval getInterval() {
        return interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }

    public void setIntervalCount(int intervalCount) {
        this.intervalCount = intervalCount;
    }

}
