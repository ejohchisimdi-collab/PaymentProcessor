package com.chisimdi.PaymentProcessor.services;

import com.chisimdi.PaymentProcessor.Exceptions.InvalidCredentialsException;
import com.chisimdi.PaymentProcessor.Exceptions.ResourceNotFoundException;
import com.chisimdi.PaymentProcessor.models.*;
import com.chisimdi.PaymentProcessor.repository.*;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);
    private PaymentRepository paymentRepository;
    private MerchantSettingRepository merchantSettingRepository;
    private MerchantAccountRepository merchantAccountRepository;
    private AccountRepository accountRepository;
    private UserRepository userRepository;
    private Random random;
    private PaymentIdempotencyRepository paymentIdempotencyRepository;
    private WebHookService webHookService;
    private WebHookRetriesRepository webHookRetriesRepository;

    public PaymentService(PaymentRepository paymentRepository, MerchantAccountRepository merchantAccountRepository,
                          MerchantSettingRepository merchantSettingRepository, AccountRepository accountRepository, UserRepository userRepository, Random random, PaymentIdempotencyRepository paymentIdempotencyRepository,
                          WebHookService webHookService,
                          WebHookRetriesRepository webHookRetriesRepository) {
        this.paymentRepository = paymentRepository;
        this.merchantAccountRepository = merchantAccountRepository;
        this.merchantSettingRepository = merchantSettingRepository;
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.random = random;
        this.paymentIdempotencyRepository = paymentIdempotencyRepository;
        this.webHookService = webHookService;
        this.webHookRetriesRepository = webHookRetriesRepository;

    }

    public PaymentDTO toPaymentDTO(Payment payment) {
        PaymentDTO paymentDTO = new PaymentDTO();
        if (payment.getPaymentStatus() != null) {
            paymentDTO.setStatus(payment.getPaymentStatus());
        }
        if (payment.getAmount() != null) {
            paymentDTO.setAmount(payment.getAmount());
        }
        if (payment.getAccountType() != null) {
            paymentDTO.setAccountType(payment.getAccountType());
        }
        if (payment.getAccount() != null) {
            paymentDTO.setAccountId(payment.getAccount().getId());
        }
        if (payment.getMerchant() != null) {
            paymentDTO.setMerchantId(payment.getMerchant().getId());
        }
        if ((payment.getWarnings() != null)) {
            paymentDTO.setWarnings(payment.getWarnings());
        }
        if (payment.getLocation() != null) {
            paymentDTO.setLocation(payment.getLocation());
        }
        paymentDTO.setLocalDate(payment.getLocalDate());
        paymentDTO.setId(payment.getId());
        return paymentDTO;
    }

    public Payment pendingProcessor(int merchantId, int accountId, BigDecimal amount, Location location) {
        log.info("Creating payments for merchant with id {}", merchantId);
        MerchantSetting merchantSetting = merchantSettingRepository.findByMerchantId(merchantId);
        Account account = accountRepository.findById(accountId).orElse(null);
        if (account == null) {
            throw new ResourceNotFoundException("Account with id " + accountId + " not found");
        }
        User user = userRepository.findByIdAndRole(merchantId, "Merchant");
        Payment payment = new Payment();
        if (merchantSetting == null) {
            throw new ResourceNotFoundException("MerchantSetting with merchant id " + merchantId + " not found");
        }
        if (user == null) {
            throw new ResourceNotFoundException("Merchant with id " + merchantId + " not found");
        }
        if (!account.getCurrency().equals(merchantSetting.getCurrency())) {
            throw new InvalidCredentialsException("Accounts currency is incompatible with merchant currency");
        }
        if (amount.compareTo(merchantSetting.getMoneyLimit()) > 0) {
            throw new InvalidCredentialsException("Amount specified is greater than the money limit");
        }

        if (account instanceof BankAccount) {
            log.debug("processing payment type bank");
            payment.setAccountType(AccountType.BANK);
            payment.setPaymentStatus(PaymentStatus.CAPTURED);
        }
        if (account instanceof CreditCard) {
            log.debug("processing payment type credit");
            payment.setAccountType(AccountType.CREDIT);
            payment.setPaymentStatus(PaymentStatus.PENDING);
        }
        log.debug("processing payment");
        payment.setAmount(amount);
        payment.setAmountLeft(amount);
        payment.setMerchant(merchantSetting.getMerchant());
        payment.setAccount(account);
        payment.setLocation(location);
        log.info("Payment processed successfully");

        WebhookEvents webhookEvents = new WebhookEvents();
        webhookEvents.setEventType(EventType.PAYMENT);
        webhookEvents.setAmount(payment.getAmount());
        webhookEvents.setPaymentStatus(payment.getPaymentStatus());
        webhookEvents.setRefundStatus(null);
        webhookEvents.setAccountType(payment.getAccountType());
        webhookEvents.setMerchant(merchantSetting.getMerchant());
        webhookEvents.setWarnings(payment.getWarnings());
        try {
            log.debug("Sending webhooks");
            webHookService.sendWebhook(merchantSetting.getMerchantEndpoint(), webhookEvents);
        } catch (RestClientException e) {
            log.warn("Webhook failed to be delivered sending retries");
            WebhookRetries webhookRetries = new WebhookRetries();
            webhookRetries.setAccountType(payment.getAccountType());
            webhookRetries.setWarnings(payment.getWarnings());
            webhookRetries.setEventType(EventType.PAYMENT);
            webhookRetries.setAmount(payment.getAmount());
            webhookRetries.setPaymentStatus(payment.getPaymentStatus());
            webhookRetries.setRefundStatus(null);
            webhookRetries.setMerchant(merchantSetting.getMerchant());
            webhookRetries.setAccountType(payment.getAccountType());
            webHookRetriesRepository.save(webhookRetries);
        }

        return paymentRepository.save(payment);


    }

    public Payment validatedProcessor(Payment payment) {

        payment.setWarnings(new ArrayList<>());
        log.info("Validating payment with id {}", payment.getId());
        if (payment.getPaymentStatus().equals(PaymentStatus.PENDING)) {
            int fraudScore = 0;


            List<Payment> paymentsList = paymentRepository.findByAccountCustomerIdAndPaymentStatus
                    (payment.getAccount().getCustomer().getId(), PaymentStatus.SETTLED);
            if (paymentsList.size() > 1) {
                if (!paymentsList.get(paymentsList.size() -1).getLocation().equals(payment.getLocation())) {
                    log.warn("Payment was made from a different location, updating fraud score");
                    fraudScore += 1;
                    String reason = "A payment from a different location was made";
                    payment.getWarnings().add(reason);
                }
            }

            if (paymentsList.size() >= 3) {
                if (ChronoUnit.MINUTES.between(paymentsList.get(paymentsList.size() - 3).getLocalDate(), paymentsList.get(paymentsList.size() - 1).getLocalDate()) < 1) {
                    log.warn("Three consecutive payments were made in one minute. Updating fraud score");
                    fraudScore += 1;
                    String reason = " Three consecutive payments were made in less than one minute";
                    payment.getWarnings().add(reason);
                }


            }
            List<Payment> paymentsThatFailed = paymentRepository.findByAccountId(payment.getAccount().getId());
            if (paymentsThatFailed.size() >= 3) {

                if (paymentsThatFailed.get(paymentsThatFailed.size() - 1).getPaymentStatus().equals(PaymentStatus.FAILED) && paymentsThatFailed.get(paymentsThatFailed.size() - 2).getPaymentStatus().equals(PaymentStatus.FAILED) && paymentsThatFailed.get(paymentsThatFailed.size() - 3).getPaymentStatus().equals(PaymentStatus.FAILED)) {
                    log.warn("Three consecutive failed payments were mad. Updating fraud score ");
                    String reason = "Three consecutive failed payments were previously made";
                    payment.getWarnings().add(reason);
                    fraudScore += 1;
                }
            }
            if (payment.getAccount().getMoneyRemaining().compareTo(payment.getAmount()) < 0) {
                log.warn("Insufficient funds updating payment status to failed ");
                String reason = "Insufficient funds";
                payment.getWarnings().add(reason);
            }
            if (fraudScore >= 3 || payment.getAccount().getMoneyRemaining().compareTo(payment.getAmount()) < 0) {
                log.warn("Fraud score is greater than or equal to three / insufficient funds. Updating fraud score to failed");
                payment.setPaymentStatus(PaymentStatus.FAILED);
            } else {
                payment.setPaymentStatus(PaymentStatus.VALIDATED);
            }
            MerchantSetting merchantSetting = merchantSettingRepository.findByMerchantId(payment.getMerchant().getId());
            WebhookEvents webhookEvents = new WebhookEvents();
            webhookEvents.setAccountType(payment.getAccountType());
            webhookEvents.setEventType(EventType.PAYMENT);
            webhookEvents.setAmount(payment.getAmount());
            webhookEvents.setPaymentStatus(payment.getPaymentStatus());
            webhookEvents.setRefundStatus(null);
            webhookEvents.setMerchant(merchantSetting.getMerchant());
            webhookEvents.setWarnings(payment.getWarnings());
            try {
                log.debug("Sending webhooks");
                webHookService.sendWebhook(merchantSetting.getMerchantEndpoint(), webhookEvents);
            } catch (RestClientException e) {
                log.warn("Webhook failed to be delivered, sending retries");
                WebhookRetries webhookRetries = new WebhookRetries();
                webhookRetries.setAccountType(payment.getAccountType());
                webhookRetries.setWarnings(payment.getWarnings());
                webhookRetries.setEventType(EventType.PAYMENT);
                webhookRetries.setAmount(payment.getAmount());
                webhookRetries.setPaymentStatus(payment.getPaymentStatus());
                webhookRetries.setRefundStatus(null);
                webhookRetries.setMerchant(merchantSetting.getMerchant());
                webhookRetries.setAccountType(payment.getAccountType());
                webHookRetriesRepository.save(webhookRetries);
            }


        }
        return paymentRepository.save(payment);
    }

    public Payment authorizationProcessor(Payment payment) {
        log.info("Authorizing payments with id {}", payment.getId());
        if (payment.getPaymentStatus().equals(PaymentStatus.VALIDATED)) {
            log.debug("Holding funds for bank account");
            Account account = payment.getAccount();
            account.setMoneyRemaining(account.getMoneyRemaining().subtract(payment.getAmount()));
            payment.setPaymentStatus(PaymentStatus.AUTHORIZED);
            log.info("Authorization successful");
            accountRepository.save(account);
            MerchantSetting merchantSetting = merchantSettingRepository.findByMerchantId(payment.getMerchant().getId());
            WebhookEvents webhookEvents = new WebhookEvents();
            webhookEvents.setAccountType(payment.getAccountType());
            webhookEvents.setEventType(EventType.PAYMENT);
            webhookEvents.setAmount(payment.getAmount());
            webhookEvents.setPaymentStatus(payment.getPaymentStatus());
            webhookEvents.setRefundStatus(null);
            webhookEvents.setMerchant(merchantSetting.getMerchant());
            webhookEvents.setWarnings(payment.getWarnings());
            try {
                log.debug("Sending webhooks");
                webHookService.sendWebhook(merchantSetting.getMerchantEndpoint(), webhookEvents);
            } catch (RestClientException e) {
                log.warn("Webhook failed to be delivered creating retries");
                WebhookRetries webhookRetries = new WebhookRetries();
                webhookRetries.setAccountType(payment.getAccountType());
                webhookRetries.setWarnings(payment.getWarnings());
                webhookRetries.setEventType(EventType.PAYMENT);
                webhookRetries.setAmount(payment.getAmount());
                webhookRetries.setPaymentStatus(payment.getPaymentStatus());
                webhookRetries.setRefundStatus(null);
                webhookRetries.setMerchant(merchantSetting.getMerchant());
                webhookRetries.setAccountType(payment.getAccountType());
                webHookRetriesRepository.save(webhookRetries);
            }

        }
        return paymentRepository.save(payment);

    }

    public Payment capturingProcessor(Payment payment) {
        log.info("Capturing payment with id {}", payment.getId());
        if (payment.getPaymentStatus().equals(PaymentStatus.AUTHORIZED)) {
            payment.setPaymentStatus(PaymentStatus.CAPTURED);
            log.info("Capturing successful");
            MerchantSetting merchantSetting = merchantSettingRepository.findByMerchantId(payment.getMerchant().getId());
            WebhookEvents webhookEvents = new WebhookEvents();
            webhookEvents.setEventType(EventType.PAYMENT);
            webhookEvents.setAmount(payment.getAmount());
            webhookEvents.setPaymentStatus(payment.getPaymentStatus());
            webhookEvents.setRefundStatus(null);
            webhookEvents.setMerchant(merchantSetting.getMerchant());
            webhookEvents.setWarnings(payment.getWarnings());
            webhookEvents.setAccountType(payment.getAccountType());
            try {
                log.debug("Sending webhooks");
                webHookService.sendWebhook(merchantSetting.getMerchantEndpoint(), webhookEvents);
            } catch (RestClientException e) {
                log.warn("Webhook failed to be delivered, Creting retries");
                WebhookRetries webhookRetries = new WebhookRetries();
                webhookRetries.setAccountType(payment.getAccountType());
                webhookRetries.setWarnings(payment.getWarnings());
                webhookRetries.setEventType(EventType.PAYMENT);
                webhookRetries.setAmount(payment.getAmount());
                webhookRetries.setPaymentStatus(payment.getPaymentStatus());
                webhookRetries.setRefundStatus(null);
                webhookRetries.setMerchant(merchantSetting.getMerchant());
                webhookRetries.setAccountType(payment.getAccountType());
                webHookRetriesRepository.save(webhookRetries);
            }

        }
        return paymentRepository.save(payment);

    }

    public Payment settledProcessor(Payment payment) {
        log.info("Settling payment with id {}", payment.getId());
        if (payment.getPaymentStatus().equals(PaymentStatus.CAPTURED)) {
            log.debug("Updating merchant account");
            MerchantAccount merchantAccount = merchantAccountRepository.findByMerchantId(payment.getMerchant().getId());
            merchantAccount.setBalance(merchantAccount.getBalance().add(payment.getAmount()));
            payment.setPaymentStatus(PaymentStatus.SETTLED);
            merchantAccountRepository.save(merchantAccount);
            MerchantSetting merchantSetting = merchantSettingRepository.findByMerchantId(payment.getMerchant().getId());
            WebhookEvents webhookEvents = new WebhookEvents();
            webhookEvents.setAccountType(payment.getAccountType());
            webhookEvents.setWarnings(payment.getWarnings());
            webhookEvents.setEventType(EventType.PAYMENT);
            webhookEvents.setAmount(payment.getAmount());
            webhookEvents.setPaymentStatus(payment.getPaymentStatus());
            webhookEvents.setRefundStatus(null);
            webhookEvents.setMerchant(merchantSetting.getMerchant());
            webhookEvents.setAccountType(payment.getAccountType());
            try {
                log.debug("Sending webhooks");
                webHookService.sendWebhook(merchantSetting.getMerchantEndpoint(), webhookEvents);
            } catch (RestClientException e) {
                log.warn("Webhook failed to be delivered sending retries");
                WebhookRetries webhookRetries = new WebhookRetries();
                webhookRetries.setAccountType(payment.getAccountType());
                webhookRetries.setWarnings(payment.getWarnings());
                webhookRetries.setEventType(EventType.PAYMENT);
                webhookRetries.setAmount(payment.getAmount());
                webhookRetries.setPaymentStatus(payment.getPaymentStatus());
                webhookRetries.setRefundStatus(null);
                webhookRetries.setMerchant(merchantSetting.getMerchant());
                webhookRetries.setAccountType(payment.getAccountType());
                webHookRetriesRepository.save(webhookRetries);
            }

        }

        return paymentRepository.save(payment);
    }

    public PaymentDTO pay(String idempotencyKey, int merchantId, int accountId, BigDecimal amount, Location location) {
        PaymentIdempotency paymentIdempotency1 = paymentIdempotencyRepository.findByIdempotencyKey(idempotencyKey);
        if (paymentIdempotency1 != null) {
            return toPaymentDTO(paymentIdempotency1.getPayment());
        }
        Payment payment = pendingProcessor(merchantId, accountId, amount, location);
        payment = validatedProcessor(payment);
        payment = authorizationProcessor(payment);
        payment = capturingProcessor(payment);
        payment = settledProcessor(payment);
        PaymentIdempotency paymentIdempotency = new PaymentIdempotency();
        paymentIdempotency.setPayment(payment);
        paymentIdempotency.setIdempotencyKey(idempotencyKey);
        paymentIdempotencyRepository.save(paymentIdempotency);
        return toPaymentDTO(payment);
    }


    public void bank() {
        log.info("Validating bank payments");
        List<Payment> payments = paymentRepository.findByAccountType(AccountType.BANK);
        for (int x = 0; x < payments.size(); x++) {
            Payment payment = payments.get(x);
            payment.setWarnings(new ArrayList<>());
            if (payment.getDone() == true) {
                continue;
            }
            int fraudScore = 0;
            List<Payment> paymentsList = paymentRepository.findByAccountCustomerIdAndPaymentStatus
                    (payment.getAccount().getCustomer().getId(), PaymentStatus.SETTLED);
            if (paymentsList.size() > 1) {
                if (!paymentsList.get(paymentsList.size() - 1).getLocation().equals(payment.getLocation())) {
                    log.warn("Payment in a different location was made ");
                    String reason = "Payment from a different location was made,Updating fraud score to one";
                    payment.getWarnings().add(reason);
                    fraudScore += 1;

                }
            }

            if (paymentsList.size() >= 4) {
                if (ChronoUnit.MINUTES.between(paymentsList.get(paymentsList.size() - 3).getLocalDate(), paymentsList.get(paymentsList.size() - 1).getLocalDate()) < 1) {
                    fraudScore += 1;
                    log.warn("Three payments were made in a very short amount of time, updating payment status");
                    String reason = "Three payments were made in a very short amount of time";
                    payment.getWarnings().add(reason);
                }


            }
            List<Payment> paymentsThatFailed = paymentRepository.findByAccountId(payment.getAccount().getId());
            if (paymentsThatFailed.size() > 3) {

                if (paymentsThatFailed.get(paymentsThatFailed.size() - 1).getPaymentStatus().equals(PaymentStatus.FAILED) && paymentsThatFailed.get(paymentsThatFailed.size() - 2).getPaymentStatus().equals(PaymentStatus.FAILED) && paymentsThatFailed.get(paymentsThatFailed.size() - 3).getPaymentStatus().equals(PaymentStatus.FAILED)) {
                    log.warn("Three consecutive failed payments, updating fraud score");
                    String reason = "Three consecutive failed payments";
                    payment.getWarnings().add(reason);
                    fraudScore += 1;
                }
            }
            if (payment.getAccount().getMoneyRemaining().compareTo(payment.getAmount()) < 0) {
                String reason = "Insufficient funds";
                log.warn("Insufficient funds, setting payment status to failed, reversing transfer");
                payment.getWarnings().add(reason);
            }

            if (fraudScore >= 3 || payment.getAccount().getMoneyRemaining().compareTo(payment.getAmount()) < 0) {
                log.warn("Fraud score is greater than or equal to three / insufficient funds. Updating fraud score to failed");
                MerchantAccount merchantAccount = merchantAccountRepository.findByMerchantId(payment.getMerchant().getId());
                merchantAccount.setBalance(merchantAccount.getBalance().subtract(payment.getAmount()));
                payment.setPaymentStatus(PaymentStatus.FAILED);
                payment.setDone(true);
                paymentRepository.save(payment);

            } else {
                Account account = accountRepository.findById(payment.getAccount().getId()).orElse(null);
                account.setMoneyRemaining(account.getMoneyRemaining().subtract(payment.getAmount()));
                accountRepository.save(account);
                payment.setDone(true);
                paymentRepository.save(payment);
            }
            MerchantSetting merchantSetting = merchantSettingRepository.findByMerchantId(payment.getMerchant().getId());
            WebhookEvents webhookEvents = new WebhookEvents();
            webhookEvents.setAccountType(payment.getAccountType());
            webhookEvents.setEventType(EventType.PAYMENT);
            webhookEvents.setAmount(payment.getAmount());
            webhookEvents.setPaymentStatus(payment.getPaymentStatus());
            webhookEvents.setRefundStatus(null);
            webhookEvents.setWarnings(payment.getWarnings());
            webhookEvents.setMerchant(merchantSetting.getMerchant());
            webHookService.sendWebhook(merchantSetting.getMerchantEndpoint(), webhookEvents);
        }


    }

    public List<PaymentDTO> findAllPayments(int pageNumber, int size) {
        Page<Payment> payments = paymentRepository.findAll(PageRequest.of(pageNumber, size));
        List<PaymentDTO> paymentDTOS = new ArrayList<>();

        for (Payment p : payments) {
            paymentDTOS.add(toPaymentDTO(p));
        }
        return paymentDTOS;

    }

    public List<PaymentDTO> findByMerchantId(int merchantId, int pageNumber, int size) {
        Page<Payment> payments = paymentRepository.findByMerchantId(merchantId, PageRequest.of(pageNumber, size));
        List<PaymentDTO> paymentDTOS = new ArrayList<>();

        for (Payment p : payments) {
            paymentDTOS.add(toPaymentDTO(p));
        }
        return paymentDTOS;
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void bankWithRetries() {
        int retries = 0;
        while (retries < 5) {
            try {
                bank();
            } catch (OptimisticLockException e) {

                retries++;
                if(retries==5)throw e;
            }

        }
    }
    @Transactional
    public PaymentDTO payWithRetries(String idempotencyKey, int merchantId, int accountId, BigDecimal amount, Location location) {
        int retries = 0;
        while (retries < 5) {
            try {
                return pay(idempotencyKey, merchantId, accountId, amount, location);
            } catch (OptimisticLockException e) {
                retries++;

            }
        }
        throw new OptimisticLockException("Payment failed after max retries");


    }
}