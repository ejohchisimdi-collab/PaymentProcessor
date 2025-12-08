package com.chisimdi.PaymentProcessor.models;

import java.math.BigDecimal;

public class MerchantAccountDTO {
    private int id;
    private BigDecimal balance;

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
