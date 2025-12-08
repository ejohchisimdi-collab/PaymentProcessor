package com.chisimdi.PaymentProcessor.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
public class BankAccount extends Account{

    @NotNull
    @Enumerated(EnumType.STRING)
   private Currency currency;
    @NotNull
   private BigDecimal balance;

    public BankAccount(Currency currency,User customer,BigDecimal balance){
        this.currency=currency;
        this.customer=customer;
        this.balance=balance;
    }
    public BankAccount(){}

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
    }

    public User getCustomer() {
        return customer;
    }

    public Currency getCurrency() {
        return currency;
    }

   public void setMoneyRemaining(BigDecimal balance){
        this.balance=balance;
   }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public BigDecimal getMoneyRemaining(){
        return balance;
    }

    @Override
    public void setVersion(int version) {
        this.version=version;
    }

    @Override
    public int getVersion() {
        return version;
    }
}
