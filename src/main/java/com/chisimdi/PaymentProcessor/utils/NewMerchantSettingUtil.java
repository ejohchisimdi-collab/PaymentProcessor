package com.chisimdi.PaymentProcessor.utils;

import com.chisimdi.PaymentProcessor.models.Currency;
import com.chisimdi.PaymentProcessor.models.RefundType;
import com.chisimdi.PaymentProcessor.models.User;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;

import java.math.BigDecimal;

public class NewMerchantSettingUtil {
    @Enumerated(EnumType.STRING)
    Currency currency;
    BigDecimal moneyLimit;
    String merchantEndpoint;
    @Enumerated(EnumType.STRING)
    RefundType refundType;

    public String getMerchantEndpoint() {
        return merchantEndpoint;
    }

    public void setMerchantEndpoint(String merchantEndpoint) {
        this.merchantEndpoint = merchantEndpoint;
    }

    public void setMoneyLimit(BigDecimal moneyLimit) {
        this.moneyLimit = moneyLimit;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public BigDecimal getMoneyLimit() {
        return moneyLimit;
    }

    public Currency getCurrency() {
        return currency;
    }

    public RefundType getRefundType() {
        return refundType;
    }

    public void setRefundType(RefundType refundType) {
        this.refundType = refundType;
    }
}
