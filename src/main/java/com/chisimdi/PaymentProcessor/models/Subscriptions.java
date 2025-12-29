package com.chisimdi.PaymentProcessor.models;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Subscriptions {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @ManyToOne
    private Prices prices;
    @ManyToOne
    private User customers;
    private int accountId;
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus subscriptionStatus;
    private LocalDate nextDueDate;
    private LocalDate previousDueDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }

    public int getAccountId() {
        return accountId;
    }

    public SubscriptionStatus getSubscriptionStatus() {
        return subscriptionStatus;
    }

    public void setCustomers(User customers) {
        this.customers = customers;
    }

    public LocalDate getNextDueDate() {
        return nextDueDate;
    }

    public LocalDate getPreviousDueDate() {
        return previousDueDate;
    }

    public Prices getPrices() {
        return prices;
    }

    public User getCustomers() {
        return customers;
    }

    public void setNextDueDate(LocalDate nextDueDate) {
        this.nextDueDate = nextDueDate;
    }

    public void setPreviousDueDate(LocalDate previousDueDate) {
        this.previousDueDate = previousDueDate;
    }

    public void setSubscriptionStatus(SubscriptionStatus subscriptionStatus) {
        this.subscriptionStatus = subscriptionStatus;
    }

    public void setPrices(Prices prices) {
        this.prices = prices;
    }
}
