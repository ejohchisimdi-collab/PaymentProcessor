package com.chisimdi.PaymentProcessor.models;

import jakarta.persistence.ManyToOne;

import java.time.LocalDate;

public class InvoiceDTO {
    private int id;
    private int merchantId;
    private int customerId;
    private int customerAccountId;
    @ManyToOne
    private Subscriptions subscriptions;
    private LocalDate createdAt=LocalDate.now();
    private InvoiceStatus invoiceStatus;

    public void setInvoiceStatus(InvoiceStatus invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public InvoiceStatus getInvoiceStatus() {
        return invoiceStatus;
    }

    public void setCustomerAccountId(int customerAccountId) {
        this.customerAccountId = customerAccountId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getCustomerAccountId() {
        return customerAccountId;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public int getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Subscriptions getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(Subscriptions subscriptions) {
        this.subscriptions = subscriptions;
    }
}
