package com.chisimdi.PaymentProcessor.models;

import jakarta.persistence.Enumerated;

import java.math.BigDecimal;

public class MerchantSettingDTO {
   private int id;
    private Currency currency;
   private BigDecimal moneyLimit;
   private String merchantEndpoint;
   private RefundType refundType;

    public void setId(int id) {
        this.id = id;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public int getId() {
        return id;
    }

    public BigDecimal getMoneyLimit() {
        return moneyLimit;
    }

    public void setMoneyLimit(BigDecimal moneyLimit) {
        this.moneyLimit = moneyLimit;
    }

    public void setMerchantEndpoint(String merchantEndpoint) {
        this.merchantEndpoint = merchantEndpoint;
    }

    public String getMerchantEndpoint() {
        return merchantEndpoint;
    }

    public void setRefundType(RefundType refundType) {
        this.refundType = refundType;
    }

    public RefundType getRefundType() {
        return refundType;
    }

}
