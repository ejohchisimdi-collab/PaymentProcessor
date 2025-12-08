package com.chisimdi.PaymentProcessor.services;

import com.chisimdi.PaymentProcessor.Exceptions.ResourceNotFoundException;
import com.chisimdi.PaymentProcessor.models.*;
import com.chisimdi.PaymentProcessor.repository.*;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class RefundService {
    private static final Logger log = LoggerFactory.getLogger(RefundService.class);
    private RefundRepository refundRepository;
    private MerchantSettingRepository merchantSettingRepository;
    private PaymentRepository paymentRepository;
    private MerchantAccountRepository merchantAccountRepository;
    private AccountRepository accountRepository;
    private IdempotencyForRefundsRepository idempotencyForRefundsRepository;
    private WebHookService webHookService;
    private WebHookRetriesRepository webHookRetriesRepository;

    public RefundService(RefundRepository refundRepository,
                         MerchantSettingRepository merchantSettingRepository,
                         PaymentRepository paymentRepository,AccountRepository accountRepository,
                         MerchantAccountRepository merchantAccountRepository, IdempotencyForRefundsRepository idempotencyForRefundsRepository,
                         WebHookService webHookService,WebHookRetriesRepository webHookRetriesRepository){
        this.accountRepository=accountRepository;
        this.refundRepository=refundRepository;
        this.merchantSettingRepository=merchantSettingRepository;
        this.merchantAccountRepository=merchantAccountRepository;
        this.paymentRepository=paymentRepository;
        this.idempotencyForRefundsRepository=idempotencyForRefundsRepository;
        this.webHookService=webHookService;
        this.webHookRetriesRepository=webHookRetriesRepository;
    }

    public RefundDTO toRefundDTO(Refund refund){
        RefundDTO refundDTO=new RefundDTO();
        if(refund.getRefundType()!=null){
            refundDTO.setRefundType(refund.getRefundType());
        }
        if(refund.getRefundStatus()!=null){
            refundDTO.setRefundStatus(refund.getRefundStatus());
        }
        if(refund.getAmount()!=null){
            refundDTO.setAmount(refund.getAmount());
        }
        if(refund.getPayment()!=null){
            refundDTO.setPaymentId(refund.getPayment().getId());
        }
        if(refund.getReasons()!=null){
            refundDTO.setReasons(refund.getReasons());
        }
        refundDTO.setLocalDateTime(refund.getLocalDateTime());
        refundDTO.setId(refund.getId());
        return refundDTO;
    }

    public Refund validated(int merchantId, int paymentId, BigDecimal amount) {
        log.info("processing refund with payment id {}",paymentId);
        log.debug("Setting refund to pending");
        Refund refund = new Refund();
        refund.setRefundStatus(RefundStatus.PENDING);
        refund.setReasons(new ArrayList<>());

        MerchantSetting merchantSetting = merchantSettingRepository.findByMerchantId(merchantId);
        Payment payment = paymentRepository.findById(paymentId).orElse(null);;
        if (merchantSetting == null) {
            throw new ResourceNotFoundException("Merchant with with id " + merchantId + " does not exist");
        }
        if (payment == null) {
            throw new ResourceNotFoundException("Payment with id not found");
        }
log.debug("processing payment");
        refund.setPayment(payment);
        refund.setAmount(amount);
        refund.setRefundType(merchantSetting.getRefundType());


        if (merchantSetting.getRefundType().equals(RefundType.COMPLETE) && payment.getAmountLeft().compareTo(amount)>0 ||
                merchantSetting.getRefundType().equals(RefundType.COMPLETE) && payment.getAmountLeft().compareTo(amount)<0) {
            refund.setRefundStatus(RefundStatus.FAILURE);
            refund.getReasons().add("Merchant setting requires complete payment but this payment is partial or has already been paid");
        log.warn("Merchant setting requires complete payment but this payment is partial or has already been paid");
        }

        if (merchantSetting.getRefundType().equals(RefundType.PARTIAL) && payment.getAmountLeft().compareTo(amount) < 0 ||merchantSetting.getRefundType().equals(RefundType.PARTIAL) && payment.getAmount().compareTo(amount)==0) {
            refund.setRefundStatus(RefundStatus.FAILURE);
            refund.getReasons().add("Merchant settings require partial payment but this payment is more than the required amount or is being made as a complete refund");
        log.warn("Merchant settings require partial payment but this payment is more than the required amount or is being made as a complete refund");
        }
        if (payment.getPaymentStatus().equals(PaymentStatus.FAILED)) {
            refund.setRefundStatus(RefundStatus.FAILURE);
            refund.getReasons().add("Refund cannot be made for a failed payment");
        log.warn("Refund cannot be made for a failed payment");
        }
        if (refund.getRefundStatus().equals(RefundStatus.FAILURE)) {

        }
        else {
            log.debug("Setting payment status to processing");
            refund.setRefundStatus(RefundStatus.PROCESSING);
        }

        WebhookEvents webhookEvents=new WebhookEvents();
        webhookEvents.setAccountType(payment.getAccountType());
        webhookEvents.setEventType(EventType.REFUND);
        webhookEvents.setAmount(refund.getAmount());
        webhookEvents.setPaymentStatus(payment.getPaymentStatus());
        webhookEvents.setRefundStatus(refund.getRefundStatus());
        webhookEvents.setWarnings(refund.getReasons());
        webhookEvents.setMerchant(merchantSetting.getMerchant());
        try {
            log.debug("Sending webhooks");
            webHookService.sendWebhook(merchantSetting.getMerchantEndpoint(),webhookEvents);
        }
        catch (RestClientException  e){
            log.warn("Webhooks failed to be delivered, sending retries");
            WebhookRetries webhookRetries=new WebhookRetries();
            webhookRetries.setAccountType(payment.getAccountType());
            webhookRetries.setEventType(EventType.REFUND);
            webhookRetries.setAmount(refund.getAmount());
            webhookRetries.setPaymentStatus(payment.getPaymentStatus());
            webhookRetries.setRefundStatus(refund.getRefundStatus());
            webhookRetries.setWarnings(refund.getReasons());
            webhookRetries.setMerchant(merchantSetting.getMerchant());
            webHookRetriesRepository.save(webhookRetries);
        }
        refundRepository.save(refund);
        return refund;
    }

    public Refund settlementProcessor(Refund refund){
        log.info("Settling payment");
        if(refund.getRefundStatus().equals(RefundStatus.PROCESSING)){
            Payment payment=refund.getPayment();
            Account account=refund.getPayment().getAccount();
            account.setMoneyRemaining(account.getMoneyRemaining().add(refund.getAmount()));
            log.debug("Crediting to customer account");
            accountRepository.save(account);
            payment.setAmountLeft(payment.getAmountLeft().subtract(refund.getAmount()));
            paymentRepository.save(payment);
            log.debug("Debiting from merchant account");
            MerchantAccount merchantAccount=merchantAccountRepository.findByMerchantId(payment.getMerchant().getId());
            merchantAccount.setBalance(merchantAccount.getBalance().subtract(refund.getAmount()));
            merchantAccountRepository.save(merchantAccount);
            refund.setRefundStatus(RefundStatus.SETTLEMENT);

        }
        Payment payment=refund.getPayment();
        MerchantSetting merchantSetting= merchantSettingRepository.findByMerchantId(payment.getMerchant().getId());
        WebhookEvents webhookEvents=new WebhookEvents();
        webhookEvents.setAccountType(payment.getAccountType());
        webhookEvents.setEventType(EventType.REFUND);
        webhookEvents.setAmount(refund.getAmount());
        webhookEvents.setPaymentStatus(payment.getPaymentStatus());
        webhookEvents.setRefundStatus(refund.getRefundStatus());
        webhookEvents.setWarnings(refund.getReasons());
        webhookEvents.setMerchant(merchantSetting.getMerchant());
        try {
            log.debug("Sending webhooks");
            webHookService.sendWebhook(merchantSetting.getMerchantEndpoint(),webhookEvents);
        }
        catch (RestClientException  e){
            log.warn("Webhook failed to be delivered, creating retries");
            WebhookRetries webhookRetries=new WebhookRetries();
            webhookRetries.setAccountType(payment.getAccountType());
            webhookRetries.setEventType(EventType.REFUND);
            webhookRetries.setAmount(refund.getAmount());
            webhookRetries.setPaymentStatus(payment.getPaymentStatus());
            webhookRetries.setRefundStatus(refund.getRefundStatus());
            webhookRetries.setWarnings(refund.getReasons());
            webhookRetries.setMerchant(merchantSetting.getMerchant());
            webHookRetriesRepository.save(webhookRetries);
        }

        return refundRepository.save(refund);

    }

    public RefundDTO makeARefund(String idempotencyKey,int merchantId, int paymentId, BigDecimal amount){
        IdempotencyForRefunds idempotencyForRefunds= idempotencyForRefundsRepository.findByIdempotencyKey(idempotencyKey);
        if(idempotencyForRefunds!=null){
            return toRefundDTO(idempotencyForRefunds.getRefund());
        }
        Refund refund=validated(merchantId,paymentId,amount);
        refund=settlementProcessor(refund);
        IdempotencyForRefunds idempotency=new IdempotencyForRefunds();
        idempotency.setIdempotencyKey(idempotencyKey);
        idempotency.setRefund(refund);
        idempotencyForRefundsRepository.save(idempotency);
        return toRefundDTO(refund);
    }
    public List<RefundDTO> findAllByMerchants(int merchantId, int pageNumber,int size){
        Page<Refund>refunds=refundRepository.findByPaymentMerchantId(merchantId, PageRequest.of(pageNumber,size));
        List<RefundDTO>refundDTOS=new ArrayList<>();
        for(Refund r:refunds){
            refundDTOS.add(toRefundDTO(r));
        }
        return refundDTOS;
    }
    public List<RefundDTO>findAllPayments(int pageNumber,int size){
        Page<Refund>refunds=refundRepository.findAll(PageRequest.of(pageNumber, size));
        List<RefundDTO>refundDTOS=new ArrayList<>();
        for(Refund r:refunds){
            refundDTOS.add(toRefundDTO(r));
        }
        return refundDTOS;
    }

    @Transactional
    public RefundDTO makeARefundWithRetries(String idempotencyKey,int merchantId, int paymentId, BigDecimal amount){
        int retries=0;
        while (retries<5) {
            try {
                return makeARefund(idempotencyKey, merchantId, paymentId, amount);
            }
            catch (OptimisticLockException e){
                retries++;
            }

        }
        throw new OptimisticLockException("Retries exceeded");
    }



}
