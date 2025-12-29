package com.chisimdi.PaymentProcessor.services;

import com.chisimdi.PaymentProcessor.Exceptions.ResourceNotFoundException;
import com.chisimdi.PaymentProcessor.models.*;
import com.chisimdi.PaymentProcessor.repository.*;
import com.chisimdi.PaymentProcessor.utils.CustomUserPrincipal;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class SubscriptionsService {
    private SubscriptionsRepository subscriptionsRepository;
    private InvoiceRepository invoiceRepository;
    private PaymentService paymentService;
    private PriceRepository priceRepository;
    private AccountRepository accountRepository;
    private UserRepository userRepository;

    public SubscriptionsService(SubscriptionsRepository subscriptionsRepository,InvoiceRepository invoiceRepository,PaymentService paymentService,PriceRepository priceRepository,
                                AccountRepository accountRepository,UserRepository userRepository){
        this.subscriptionsRepository=subscriptionsRepository;
        this.paymentService=paymentService;
        this.invoiceRepository=invoiceRepository;
        this.priceRepository=priceRepository;
        this.accountRepository=accountRepository;
        this.userRepository=userRepository;
    }

    public SubscriptionsDTO toSubscriptionsDTO(Subscriptions subscriptions){
        SubscriptionsDTO subscriptionsDTO=new SubscriptionsDTO();
        subscriptionsDTO.setId(subscriptions.getId());
        if(subscriptions.getPrices()!=null){
            subscriptionsDTO.setPriceId(subscriptions.getPrices().getId());
        }
        if(subscriptions.getCustomers()!=null){
            subscriptionsDTO.setCustomersId(subscriptions.getCustomers().getId());
        }
        subscriptionsDTO.setAccountId(subscriptions.getAccountId());
        if(subscriptions.getSubscriptionStatus()!=null){
            subscriptionsDTO.setSubscriptionStatus(subscriptions.getSubscriptionStatus());
        }
        if(subscriptions.getNextDueDate()!=null){
            subscriptionsDTO.setNextDueDate(subscriptions.getNextDueDate());
        }
        if (subscriptions.getPreviousDueDate()!=null){
    subscriptionsDTO.setPreviousDueDate(subscriptions.getPreviousDueDate());
        }
           return subscriptionsDTO;
    }

    public SubscriptionsDTO makeASubscription(int accountId,int customerId,int priceId){
        Account account=accountRepository.findById(accountId).orElseThrow(()->new ResourceNotFoundException("Account with Id "+accountId+" not found"));
        User user=userRepository.findByIdAndRole(customerId,"Customer");
        Prices prices=priceRepository.findById(priceId).orElseThrow(()->new ResourceNotFoundException("Price with id "+priceId+" not found"));

        Subscriptions subscriptions=new Subscriptions();
        subscriptions.setCustomers(user);
        subscriptions.setPreviousDueDate(LocalDate.now());
        subscriptions.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        subscriptions.setAccountId(account.getId());
        subscriptions.setPrices(prices);

        subscriptionsRepository.save(subscriptions);
        return toSubscriptionsDTO(subscriptions);
    }

    @Scheduled(cron = "9 * * * * *")
    public void setSubscriptionDueDates(){
        List<Subscriptions>subscriptions=subscriptionsRepository.findBySubscriptionStatus(SubscriptionStatus.ACTIVE);
        List<Subscriptions>subscriptionsToBeSaved=new ArrayList<>();
        for(int x=0;x<subscriptions.size();x++){
            Subscriptions subscriptionsLooped= subscriptions.get(x);
            Prices prices= subscriptionsLooped.getPrices();

            if(prices.getPaymentInterval().equals(PaymentInterval.WEEKLY)){
                subscriptionsLooped.setNextDueDate(subscriptionsLooped.getPreviousDueDate().plusWeeks(prices.getIntervalCount()));
            }
            if(prices.getPaymentInterval().equals(PaymentInterval.MONTHLY)){
                subscriptionsLooped.setNextDueDate(subscriptionsLooped.getPreviousDueDate().plusMonths(prices.getIntervalCount()));
            }
            if(prices.getPaymentInterval().equals(PaymentInterval.YEARLY)){
                subscriptionsLooped.setNextDueDate(subscriptionsLooped.getPreviousDueDate().plusYears(prices.getIntervalCount()));
            }
            subscriptionsToBeSaved.add(subscriptionsLooped);

        }
        subscriptionsRepository.saveAll(subscriptionsToBeSaved);
    }

    @Scheduled(cron = "10 * * * * *")
    public void  invoiceProcessor(){
    List<Subscriptions>subscriptions=subscriptionsRepository.findBySubscriptionStatus(SubscriptionStatus.ACTIVE);
        List<Invoice>invoices=new ArrayList<>();
    for(int x=0;x<subscriptions.size();x++){
        Subscriptions subscriptionsLooped= subscriptions.get(x);
        Invoice invoice=invoiceRepository.findBySubscriptionsIdAndIsPaid(subscriptionsLooped.getId(),false);
        if(invoice==null){
            Invoice invoice1=new Invoice();
            invoice1.setInvoiceStatus(InvoiceStatus.DRAFT);
            invoice1.setCreatedAt(LocalDate.now());
            invoice1.setCustomerAccountId(subscriptionsLooped.getAccountId());
            invoice1.setSubscriptions(subscriptionsLooped);
            invoice1.setMerchantId(subscriptionsLooped.getPrices().getMerchant().getId());
            invoice1.setPaid(false);
            invoices.add(invoice1);
        }
        else {
            if(LocalDate.now().isEqual(invoice.getSubscriptions().getNextDueDate())){
                invoice.setInvoiceStatus(InvoiceStatus.FINALIZED);
                invoiceRepository.save(invoice);
                invoices.add(invoice);
            }
        }
    }
    invoiceRepository.saveAll(invoices);

    }

    @Scheduled(cron = "10 * * * * *")
    public void subscribedPayments(){
        List<Invoice>invoices=invoiceRepository.findByInvoiceStatusAndIsPaid(InvoiceStatus.FINALIZED,false);
        for(Invoice invoice: invoices){
            paymentService.processPaymentForSubscriptions(invoice.getMerchantId(), invoice.getCustomerAccountId(), invoice.getSubscriptions().getPrices().getAmount(),Location.Subscription_Location,invoice.getSubscriptions().getId());
            invoice.setPaid(true);
            invoiceRepository.save(invoice);
        }
    }

    public List<SubscriptionsDTO> findAllSubscriptions(int pageNumber,int size){
        Page<Subscriptions>subscriptions=subscriptionsRepository.findAll(PageRequest.of(pageNumber, size));
        List<SubscriptionsDTO>subscriptionsDTOS=new ArrayList<>();

        for(Subscriptions s:subscriptions){
            subscriptionsDTOS.add(toSubscriptionsDTO(s));
        }
        return subscriptionsDTOS;
    }
    public List<SubscriptionsDTO>findSubscriptionsByCustomer(int customerId,int pageNumber,int size){
        Page<Subscriptions>subscriptions=subscriptionsRepository.findByCustomersId(customerId,PageRequest.of(pageNumber, size));
        List<SubscriptionsDTO>subscriptionsDTOS=new ArrayList<>();

        for(Subscriptions s:subscriptions){
            subscriptionsDTOS.add(toSubscriptionsDTO(s));
        }
        return subscriptionsDTOS;
    }
    public List<SubscriptionsDTO>findByMerchant(int merchantId,int pageNumber,int size){
        Page<Subscriptions>subscriptions=subscriptionsRepository.findByPricesMerchantId(merchantId,PageRequest.of(pageNumber, size));
        List<SubscriptionsDTO>subscriptionsDTOS=new ArrayList<>();

        for(Subscriptions s:subscriptions){
            subscriptionsDTOS.add(toSubscriptionsDTO(s));
        }
        return subscriptionsDTOS;
    }

    public SubscriptionsDTO cancelSubscription(int userId,int subscriptionId){
        Subscriptions subscriptions=subscriptionsRepository.findByIdAndCustomersId(subscriptionId,userId).orElseThrow(()->new ResourceNotFoundException("Subscription with id "+subscriptionId+" and customer Id "+userId+" not found"));
        subscriptions.setSubscriptionStatus(SubscriptionStatus.CANCELLED);
        subscriptionsRepository.save(subscriptions);
        return toSubscriptionsDTO(subscriptions);
    }
}
