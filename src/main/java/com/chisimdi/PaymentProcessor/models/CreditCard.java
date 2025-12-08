package com.chisimdi.PaymentProcessor.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
public class CreditCard extends Account{

    @NotNull
    @Column(precision = 16,scale = 2)
   private BigDecimal creditLimit;
    @Column(precision = 16,scale = 2)
    private BigDecimal totalCreditRemaining;
    @NotNull
    @Enumerated(EnumType.STRING)
    private Currency currency;


    public CreditCard(BigDecimal creditLimit,BigDecimal totalCreditRemaining, Currency currency,User customer){
        this.creditLimit=creditLimit;
        this.totalCreditRemaining=totalCreditRemaining;
        this.currency=currency;
    }
    public CreditCard(){}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public BigDecimal getMoneyRemaining() {
        return totalCreditRemaining;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public void setMoneyRemaining(BigDecimal totalCreditRemaining) {
        this.totalCreditRemaining = totalCreditRemaining;
    }

    public Currency getCurrency() {
        return currency;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public void setVersion(int version) {
        this.version=version;
    }
}
