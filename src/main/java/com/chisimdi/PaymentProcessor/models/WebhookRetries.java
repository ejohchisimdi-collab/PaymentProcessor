package com.chisimdi.PaymentProcessor.models;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class WebhookRetries {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private int retryCount=0;
    private int maxRetries=5;
    private LocalDateTime nextRetry=LocalDateTime.now().plusSeconds(5);
    private Boolean failed=false;
    private AccountType accountType;
    @Enumerated(EnumType.STRING)
    private EventType eventType;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    private RefundStatus refundStatus;
    private LocalDate localDate=LocalDate.now();
    private BigDecimal amount;
    private List<String> warnings;
    @ManyToOne
    private User merchant;
    private Boolean done=false;

    public void setRetryCount(int retryCount) {
        this.retryCount = retryCount;
    }

    public void setNextRetry(LocalDateTime nextRetry) {
        this.nextRetry = nextRetry;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public Boolean getFailed() {
        return failed;
    }

    public LocalDateTime getNextRetry() {
        return nextRetry;
    }


    public void setFailed(Boolean failed) {
        this.failed = failed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }



    public AccountType getAccountType() {
        return accountType;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setLocalDate(LocalDate localDate) {
        this.localDate = localDate;
    }

    public LocalDate getLocalDate() {
        return localDate;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public RefundStatus getRefundStatus() {
        return refundStatus;
    }

    public void setRefundStatus(RefundStatus refundStatus) {
        this.refundStatus = refundStatus;
    }

    public void setWarnings(List<String> warnings) {
        this.warnings = warnings;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public EventType getEventType() {
        return eventType;
    }

    public User getMerchant() {
        return merchant;
    }

    public void setMerchant(User merchant) {
        this.merchant = merchant;
    }

    public void setDone(Boolean done) {
        this.done = done;
    }

    public Boolean getDone() {
        return done;
    }

}
