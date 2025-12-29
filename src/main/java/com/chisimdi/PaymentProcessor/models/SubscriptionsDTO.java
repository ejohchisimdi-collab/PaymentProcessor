package com.chisimdi.PaymentProcessor.models;

import jakarta.persistence.ManyToOne;

import java.time.LocalDate;

public class SubscriptionsDTO {
    private int id;
    private int priceId;
    private int customersId;
    private int accountId;
    private SubscriptionStatus subscriptionStatus;
    private LocalDate nextDueDate;
    private LocalDate previousDueDate;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setSubscriptionStatus(SubscriptionStatus subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public SubscriptionStatus getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setPreviousDueDate(LocalDate previousDueDate) {
        this.previousDueDate = previousDueDate;
    }

    public void setNextDueDate(LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public LocalDate getPreviousDueDate() {
        return previousDueDate;
    }

    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public int getAccountId() {
        return accountId;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getCustomersId() {
        return customersId;
    }

    public int getPriceId() {
        return priceId;
    }

    public void setCustomersId(int customersId) {
        this.customersId = customersId;
    }

    public void setPriceId(int priceId) {
        this.priceId = priceId;
    }

}
