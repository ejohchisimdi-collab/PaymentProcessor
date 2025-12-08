package com.chisimdi.PaymentProcessor.models;

import jakarta.persistence.Entity;

import java.math.BigDecimal;

public class CreditCardDTO {
   private int id;
    private BigDecimal creditLimit;
    private BigDecimal totalCreditRemaining;
    private Currency currency;

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public void setTotalCreditRemaining(BigDecimal totalCreditRemaining) {
        this.totalCreditRemaining = totalCreditRemaining;
    }

    public BigDecimal getTotalCreditRemaining() {
        return totalCreditRemaining;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

}
