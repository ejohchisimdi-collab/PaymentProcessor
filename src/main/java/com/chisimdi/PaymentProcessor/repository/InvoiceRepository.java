package com.chisimdi.PaymentProcessor.repository;

import com.chisimdi.PaymentProcessor.models.Invoice;
import com.chisimdi.PaymentProcessor.models.InvoiceStatus;
import com.chisimdi.PaymentProcessor.models.SubscriptionStatus;
import com.chisimdi.PaymentProcessor.models.Subscriptions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice,Integer> {
    Invoice findBySubscriptionsIdAndIsPaid(int subscriptionsId, Boolean isPaid);
    List<Invoice>findByInvoiceStatusAndIsPaid(InvoiceStatus invoiceStatus,Boolean isPaid);
}
