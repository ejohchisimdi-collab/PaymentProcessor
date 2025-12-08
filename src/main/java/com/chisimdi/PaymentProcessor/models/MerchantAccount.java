package com.chisimdi.PaymentProcessor.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Entity
public class MerchantAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
  private   int id;
    @OneToOne
    private User merchant;
    @NotNull
    @Column(precision = 16,scale = 2)
    private BigDecimal balance;
    @Version
    private int version;

    public void setMerchant(User merchant) {
        this.merchant = merchant;
    }

    public User getMerchant() {
        return merchant;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }
}
