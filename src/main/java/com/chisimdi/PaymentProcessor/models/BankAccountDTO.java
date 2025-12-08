package com.chisimdi.PaymentProcessor.models;

import java.math.BigDecimal;

public class BankAccountDTO {
   private int  id;
    private Currency currency;
   private BigDecimal balance;

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

}
