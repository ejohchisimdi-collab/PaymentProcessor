package com.chisimdi.PaymentProcessor.models;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class Refund {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
   private int id;
    @Column(precision = 16,scale = 2)
   private BigDecimal amount;
    @ManyToOne
   private Payment payment;
    @Enumerated(EnumType.STRING)
    private RefundStatus refundStatus;
    @Enumerated(EnumType.STRING)
    private RefundType refundType;
    private List<String>reasons;
    private LocalDateTime localDateTime=LocalDateTime.now();
    @Version
    private int version;

    public RefundType getRefundType() {
        return refundType;
    }

    public void setRefundType(RefundType refundType) {
        this.refundType = refundType;
    }

    public Payment getPayment() {
        return payment;
    }

    public int getId() {
        return id;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public void setId(int id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public RefundStatus getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(RefundStatus refundStatus) {
        this.refundStatus = refundStatus;
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public List<String> getReasons() {
        return reasons;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public void setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getVersion() {
        return version;
    }
}
