package com.chisimdi.PaymentProcessor.models;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;
    @ManyToOne
    User customer;
    @Version
     int version;

    public abstract User getCustomer();

    public abstract void setCustomer(User customer);

    public abstract void setId(int id);

public abstract void setMoneyRemaining(BigDecimal bigDecimal);
    public abstract BigDecimal getMoneyRemaining();
    public abstract int getId();
    public abstract void setCurrency(Currency currency);
    public abstract Currency getCurrency();

    public abstract int getVersion();


    public abstract void setVersion(int version);
}
