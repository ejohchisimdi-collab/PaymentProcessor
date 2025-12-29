package com.chisimdi.PaymentProcessor.models;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int merchantId;
    private int customerId;
    private int customerAccountId;
    private LocalDate createdAt=LocalDate.now();
    @Enumerated(EnumType.STRING)
    private InvoiceStatus invoiceStatus;
    @ManyToOne
    private Subscriptions subscriptions;
    private Boolean isPaid=false;

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public int getMerchantId() {
        return merchantId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public InvoiceStatus getInvoiceStatus() {
        return invoiceStatus;
    }

    public int getCustomerAccountId() {
        return customerAccountId;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCustomerAccountId(int customerAccountId) {
        this.customerAccountId = customerAccountId;
    }

    public void setInvoiceStatus(InvoiceStatus invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }

    public Boolean getPaid() {
        return isPaid;
    }

    public void setPaid(Boolean paid) {
        isPaid = paid;
    }

    public void setSubscriptions(Subscriptions subscriptions) {
        this.subscriptions = subscriptions;
    }

    public Subscriptions getSubscriptions() {
        return subscriptions;
    }
}
