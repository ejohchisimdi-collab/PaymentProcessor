package com.chisimdi.PaymentProcessor.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
@Entity
public class MerchantSetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
  private   int id;
    @NotNull
    @Enumerated(EnumType.STRING)
  private   Currency currency;
    @OneToOne
  private   User merchant;
    @NotNull
    @Column(precision = 16,scale = 2)
  private BigDecimal moneyLimit;

  private   String merchantEndpoint="http://localhost:8080/webhooks/notifications";
    @NotNull
    @Enumerated(EnumType.STRING)
    private RefundType refundType;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User getMerchant() {
        return merchant;
    }

    public void setMerchant(User merchant) {
        this.merchant = merchant;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setMoneyLimit(BigDecimal moneyLimit) {
        this.moneyLimit = moneyLimit;
    }

    public BigDecimal getMoneyLimit() {
        return moneyLimit;
    }

    public String getMerchantEndpoint() {
        return merchantEndpoint;
    }

    public void setMerchantEndpoint(String merchantEndpoint) {
        this.merchantEndpoint = merchantEndpoint;
    }

    public RefundType getRefundType() {
        return refundType;
    }

    public void setRefundType(RefundType refundType) {
        this.refundType = refundType;
    }
}
