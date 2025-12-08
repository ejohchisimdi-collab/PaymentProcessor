package com.chisimdi.PaymentProcessor.utils;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class RefundUtil {
    @NotNull
    @Positive
   private int merchantId;
    @NotNull
    @Positive
   private int paymentId;
    @NotNull
    @Positive
   private BigDecimal amount;

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public int getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

}
