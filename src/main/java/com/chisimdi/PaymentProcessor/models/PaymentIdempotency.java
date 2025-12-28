package com.chisimdi.PaymentProcessor.models;

import jakarta.persistence.*;

@Entity
public class PaymentIdempotency {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
   private int id;
    @Column(unique = true)
   private String idempotencyKey;
    @ManyToOne
   private Payment payment;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

}
