package com.chisimdi.PaymentProcessor;

import com.chisimdi.PaymentProcessor.Exceptions.ResourceNotFoundException;
import com.chisimdi.PaymentProcessor.models.*;
import com.chisimdi.PaymentProcessor.repository.*;
import com.chisimdi.PaymentProcessor.services.RefundService;
import com.chisimdi.PaymentProcessor.services.WebHookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

import java.lang.module.ResolutionException;
import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefundServiceTest {
@Mock
    private RefundRepository refundRepository;
@Mock
private MerchantSettingRepository merchantSettingRepository;
@Mock
private PaymentRepository paymentRepository;
@Mock
private MerchantAccountRepository merchantAccountRepository;
@Mock
private AccountRepository accountRepository;
@Mock
private IdempotencyForRefundsRepository idempotencyForRefundsRepository;
@Mock
private WebHookService webHookService;
@Mock
private WebHookRetriesRepository webHookRetriesRepository;
@InjectMocks
    RefundService refundService;

@Test
    void validatedProcessorTestCompleteRefundSucess(){
    MerchantSetting merchantSetting = new MerchantSetting();
    merchantSetting.setMoneyLimit(new BigDecimal(1000));
    merchantSetting.setCurrency(Currency.USD);
    merchantSetting.setMerchantEndpoint("http://localhost");
merchantSetting.setRefundType(RefundType.COMPLETE);

    MerchantAccount merchantAccount=new MerchantAccount();
    merchantAccount.setBalance(BigDecimal.valueOf(2000));

    User merchant=new User();
    merchant.setId(1);

    merchantSetting.setMerchant(merchant);

    Payment payment=new Payment();
    payment.setPaymentStatus(PaymentStatus.SETTLED);
    payment.setLocation(Location.Australia);
    payment.setId(1);
    payment.setAmountLeft(BigDecimal.valueOf(2000));
    payment.setMerchant(merchant);

    User user=new User();
    user.setId(1);

    CreditCard creditCard=new CreditCard();
    creditCard.setId(1);
    creditCard.setMoneyRemaining(BigDecimal.valueOf(5000));
    creditCard.setCustomer(user);
    payment.setAccount(creditCard);
    payment.setAmount(BigDecimal.valueOf(2000));

    when(merchantSettingRepository.findByMerchantId(1)).thenReturn(merchantSetting);
    when(paymentRepository.findById(2)).thenReturn(Optional.of(payment));
    when(refundRepository.save(any(Refund.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
   doNothing().when(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));

    Refund refund=refundService.validated(1,2,BigDecimal.valueOf(2000));

    assertThat(refund.getRefundStatus()).isEqualTo(RefundStatus.PROCESSING);

    verify(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));
    verify(paymentRepository).findById(2);
    verify(refundRepository).save(any(Refund.class));
    verify(merchantSettingRepository).findByMerchantId(1);

}

    @Test
    void validatedProcessorTestCompleteRefundFailed_AMountWasPartial(){
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(1000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");
        merchantSetting.setRefundType(RefundType.COMPLETE);

        MerchantAccount merchantAccount=new MerchantAccount();
        merchantAccount.setBalance(BigDecimal.valueOf(2000));

        User merchant=new User();
        merchant.setId(1);

        merchantSetting.setMerchant(merchant);

        Payment payment=new Payment();
        payment.setPaymentStatus(PaymentStatus.SETTLED);
        payment.setLocation(Location.Australia);
        payment.setId(1);
        payment.setAmountLeft(BigDecimal.valueOf(2000));
        payment.setMerchant(merchant);

        User user=new User();
        user.setId(1);

        CreditCard creditCard=new CreditCard();
        creditCard.setId(1);
        creditCard.setMoneyRemaining(BigDecimal.valueOf(5000));
        creditCard.setCustomer(user);
        payment.setAccount(creditCard);
        payment.setAmount(BigDecimal.valueOf(2000));

        when(merchantSettingRepository.findByMerchantId(1)).thenReturn(merchantSetting);
        when(paymentRepository.findById(2)).thenReturn(Optional.of(payment));
        when(refundRepository.save(any(Refund.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        doNothing().when(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));

        Refund refund=refundService.validated(1,2,BigDecimal.valueOf(200));

        assertThat(refund.getRefundStatus()).isEqualTo(RefundStatus.FAILURE);
assertThat(refund.getReasons().get(0)).isEqualTo("Merchant setting requires complete payment but this payment is partial or has already been paid");

        verify(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));
        verify(paymentRepository).findById(2);
        verify(refundRepository).save(any(Refund.class));
        verify(merchantSettingRepository).findByMerchantId(1);

    }
    @Test
    void validatedProcessorTestCompleteRefundFailed_AMountWasPaid(){
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(1000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");
        merchantSetting.setRefundType(RefundType.COMPLETE);

        MerchantAccount merchantAccount=new MerchantAccount();
        merchantAccount.setBalance(BigDecimal.valueOf(2000));

        User merchant=new User();
        merchant.setId(1);

        merchantSetting.setMerchant(merchant);

        Payment payment=new Payment();
        payment.setPaymentStatus(PaymentStatus.SETTLED);
        payment.setLocation(Location.Australia);
        payment.setId(1);
        payment.setAmountLeft(BigDecimal.valueOf(0));
        payment.setMerchant(merchant);

        User user=new User();
        user.setId(1);

        CreditCard creditCard=new CreditCard();
        creditCard.setId(1);
        creditCard.setMoneyRemaining(BigDecimal.valueOf(5000));
        creditCard.setCustomer(user);
        payment.setAccount(creditCard);
        payment.setAmount(BigDecimal.valueOf(2000));

        when(merchantSettingRepository.findByMerchantId(1)).thenReturn(merchantSetting);
        when(paymentRepository.findById(2)).thenReturn(Optional.of(payment));
        when(refundRepository.save(any(Refund.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        doNothing().when(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));

        Refund refund=refundService.validated(1,2,BigDecimal.valueOf(200));

        assertThat(refund.getRefundStatus()).isEqualTo(RefundStatus.FAILURE);
        assertThat(refund.getReasons().get(0)).isEqualTo("Merchant setting requires complete payment but this payment is partial or has already been paid");

        verify(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));
        verify(paymentRepository).findById(2);
        verify(refundRepository).save(any(Refund.class));
        verify(merchantSettingRepository).findByMerchantId(1);

    }

    @Test
    void validatedProcessorTestPartialRefundSuccess(){
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(1000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");
        merchantSetting.setRefundType(RefundType.PARTIAL);

        MerchantAccount merchantAccount=new MerchantAccount();
        merchantAccount.setBalance(BigDecimal.valueOf(2000));

        User merchant=new User();
        merchant.setId(1);

        merchantSetting.setMerchant(merchant);

        Payment payment=new Payment();
        payment.setPaymentStatus(PaymentStatus.SETTLED);
        payment.setLocation(Location.Australia);
        payment.setId(1);
        payment.setAmountLeft(BigDecimal.valueOf(2000));
        payment.setMerchant(merchant);

        User user=new User();
        user.setId(1);

        CreditCard creditCard=new CreditCard();
        creditCard.setId(1);
        creditCard.setMoneyRemaining(BigDecimal.valueOf(5000));
        creditCard.setCustomer(user);
        payment.setAccount(creditCard);
        payment.setAmount(BigDecimal.valueOf(2000));

        when(merchantSettingRepository.findByMerchantId(1)).thenReturn(merchantSetting);
        when(paymentRepository.findById(2)).thenReturn(Optional.of(payment));
        when(refundRepository.save(any(Refund.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        doNothing().when(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));

        Refund refund=refundService.validated(1,2,BigDecimal.valueOf(200));

        assertThat(refund.getRefundStatus()).isEqualTo(RefundStatus.PROCESSING);

        verify(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));
        verify(paymentRepository).findById(2);
        verify(refundRepository).save(any(Refund.class));
        verify(merchantSettingRepository).findByMerchantId(1);

    }
    @Test
    void validatedProcessorTestPartialRefundFailed_AMountWasPaidOnce(){
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(1000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");
        merchantSetting.setRefundType(RefundType.PARTIAL);

        MerchantAccount merchantAccount=new MerchantAccount();
        merchantAccount.setBalance(BigDecimal.valueOf(2000));

        User merchant=new User();
        merchant.setId(1);

        merchantSetting.setMerchant(merchant);

        Payment payment=new Payment();
        payment.setPaymentStatus(PaymentStatus.SETTLED);
        payment.setLocation(Location.Australia);
        payment.setId(1);
        payment.setAmountLeft(BigDecimal.valueOf(2000));
        payment.setMerchant(merchant);

        User user=new User();
        user.setId(1);

        CreditCard creditCard=new CreditCard();
        creditCard.setId(1);
        creditCard.setMoneyRemaining(BigDecimal.valueOf(5000));
        creditCard.setCustomer(user);
        payment.setAccount(creditCard);
        payment.setAmount(BigDecimal.valueOf(2000));

        when(merchantSettingRepository.findByMerchantId(1)).thenReturn(merchantSetting);
        when(paymentRepository.findById(2)).thenReturn(Optional.of(payment));
        when(refundRepository.save(any(Refund.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        doNothing().when(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));

        Refund refund=refundService.validated(1,2,BigDecimal.valueOf(2000));

        assertThat(refund.getRefundStatus()).isEqualTo(RefundStatus.FAILURE);
        assertThat(refund.getReasons().get(0)).isEqualTo("Merchant settings require partial payment but this payment is more than the required amount or is being made as a complete refund");

        verify(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));
        verify(paymentRepository).findById(2);
        verify(refundRepository).save(any(Refund.class));
        verify(merchantSettingRepository).findByMerchantId(1);

    }

    @Test
    void validatedProcessorTestPartialRefundFailed_AMountExceededMaxAmount(){
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(1000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");
        merchantSetting.setRefundType(RefundType.PARTIAL);

        MerchantAccount merchantAccount=new MerchantAccount();
        merchantAccount.setBalance(BigDecimal.valueOf(2000));

        User merchant=new User();
        merchant.setId(1);

        merchantSetting.setMerchant(merchant);

        Payment payment=new Payment();
        payment.setPaymentStatus(PaymentStatus.SETTLED);
        payment.setLocation(Location.Australia);
        payment.setId(1);
        payment.setAmountLeft(BigDecimal.valueOf(2000));
        payment.setMerchant(merchant);

        User user=new User();
        user.setId(1);

        CreditCard creditCard=new CreditCard();
        creditCard.setId(1);
        creditCard.setMoneyRemaining(BigDecimal.valueOf(5000));
        creditCard.setCustomer(user);
        payment.setAccount(creditCard);
        payment.setAmount(BigDecimal.valueOf(2000));

        when(merchantSettingRepository.findByMerchantId(1)).thenReturn(merchantSetting);
        when(paymentRepository.findById(2)).thenReturn(Optional.of(payment));
        when(refundRepository.save(any(Refund.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        doNothing().when(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));

        Refund refund=refundService.validated(1,2,BigDecimal.valueOf(20001));

        assertThat(refund.getRefundStatus()).isEqualTo(RefundStatus.FAILURE);
        assertThat(refund.getReasons().get(0)).isEqualTo("Merchant settings require partial payment but this payment is more than the required amount or is being made as a complete refund");

        verify(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));
        verify(paymentRepository).findById(2);
        verify(refundRepository).save(any(Refund.class));
        verify(merchantSettingRepository).findByMerchantId(1);

    }
    @Test
    void validatedProcessorTestRefundFailed_PaymentStatusEqualsFailed(){
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(1000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");
        merchantSetting.setRefundType(RefundType.PARTIAL);

        MerchantAccount merchantAccount=new MerchantAccount();
        merchantAccount.setBalance(BigDecimal.valueOf(2000));

        User merchant=new User();
        merchant.setId(1);

        merchantSetting.setMerchant(merchant);

        Payment payment=new Payment();
        payment.setPaymentStatus(PaymentStatus.FAILED);
        payment.setLocation(Location.Australia);
        payment.setId(1);
        payment.setAmountLeft(BigDecimal.valueOf(2000));
        payment.setMerchant(merchant);

        User user=new User();
        user.setId(1);

        CreditCard creditCard=new CreditCard();
        creditCard.setId(1);
        creditCard.setMoneyRemaining(BigDecimal.valueOf(5000));
        creditCard.setCustomer(user);
        payment.setAccount(creditCard);
        payment.setAmount(BigDecimal.valueOf(2000));

        when(merchantSettingRepository.findByMerchantId(1)).thenReturn(merchantSetting);
        when(paymentRepository.findById(2)).thenReturn(Optional.of(payment));
        when(refundRepository.save(any(Refund.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        doNothing().when(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));

        Refund refund=refundService.validated(1,2,BigDecimal.valueOf(200));

        assertThat(refund.getRefundStatus()).isEqualTo(RefundStatus.FAILURE);
        assertThat(refund.getReasons().get(0)).isEqualTo("Refund cannot be made for a failed payment");


        verify(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));
        verify(paymentRepository).findById(2);
        verify(refundRepository).save(any(Refund.class));
        verify(merchantSettingRepository).findByMerchantId(1);

    }

    @Test
    void validatedProcessorTestRefundFailed_MerchantSettingNotFound(){
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(1000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");
        merchantSetting.setRefundType(RefundType.PARTIAL);

        MerchantAccount merchantAccount=new MerchantAccount();
        merchantAccount.setBalance(BigDecimal.valueOf(2000));

        User merchant=new User();
        merchant.setId(1);

        merchantSetting.setMerchant(merchant);

        Payment payment=new Payment();
        payment.setPaymentStatus(PaymentStatus.FAILED);
        payment.setLocation(Location.Australia);
        payment.setId(1);
        payment.setAmountLeft(BigDecimal.valueOf(2000));
        payment.setMerchant(merchant);

        User user=new User();
        user.setId(1);

        CreditCard creditCard=new CreditCard();
        creditCard.setId(1);
        creditCard.setMoneyRemaining(BigDecimal.valueOf(5000));
        creditCard.setCustomer(user);
        payment.setAccount(creditCard);
        payment.setAmount(BigDecimal.valueOf(2000));

        when(merchantSettingRepository.findByMerchantId(1)).thenReturn(null);
        when(paymentRepository.findById(2)).thenReturn(Optional.of(payment));

        assertThatThrownBy(()->refundService.validated(1,2,BigDecimal.valueOf(200))).isInstanceOf(ResourceNotFoundException.class);



        verify(webHookService,never()).sendWebhook(anyString(),any(WebhookEvents.class));
        verify(paymentRepository).findById(2);
        verify(refundRepository,never()).save(any(Refund.class));
        verify(merchantSettingRepository).findByMerchantId(1);

    }
    @Test
    void validatedProcessorTestRefundFailed_PaymentNotFound(){
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(1000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");
        merchantSetting.setRefundType(RefundType.PARTIAL);

        MerchantAccount merchantAccount=new MerchantAccount();
        merchantAccount.setBalance(BigDecimal.valueOf(2000));

        User merchant=new User();
        merchant.setId(1);

        merchantSetting.setMerchant(merchant);

        Payment payment=new Payment();
        payment.setPaymentStatus(PaymentStatus.FAILED);
        payment.setLocation(Location.Australia);
        payment.setId(1);
        payment.setAmountLeft(BigDecimal.valueOf(2000));
        payment.setMerchant(merchant);

        User user=new User();
        user.setId(1);

        CreditCard creditCard=new CreditCard();
        creditCard.setId(1);
        creditCard.setMoneyRemaining(BigDecimal.valueOf(5000));
        creditCard.setCustomer(user);
        payment.setAccount(creditCard);
        payment.setAmount(BigDecimal.valueOf(2000));

        when(merchantSettingRepository.findByMerchantId(1)).thenReturn(merchantSetting);
        when(paymentRepository.findById(2)).thenReturn(Optional.empty());

        assertThatThrownBy(()->refundService.validated(1,2,BigDecimal.valueOf(200))).isInstanceOf(ResourceNotFoundException.class);



        verify(webHookService,never()).sendWebhook(anyString(),any(WebhookEvents.class));
        verify(paymentRepository).findById(2);
        verify(refundRepository,never()).save(any(Refund.class));
        verify(merchantSettingRepository).findByMerchantId(1);

    }
    @Test
    void validatedProcessorTestPartialRefundFailed_WebhookNotSent(){
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(1000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");
        merchantSetting.setRefundType(RefundType.PARTIAL);

        MerchantAccount merchantAccount=new MerchantAccount();
        merchantAccount.setBalance(BigDecimal.valueOf(2000));

        User merchant=new User();
        merchant.setId(1);

        merchantSetting.setMerchant(merchant);

        Payment payment=new Payment();
        payment.setPaymentStatus(PaymentStatus.SETTLED);
        payment.setLocation(Location.Australia);
        payment.setId(1);
        payment.setAmountLeft(BigDecimal.valueOf(2000));
        payment.setMerchant(merchant);

        User user=new User();
        user.setId(1);

        CreditCard creditCard=new CreditCard();
        creditCard.setId(1);
        creditCard.setMoneyRemaining(BigDecimal.valueOf(5000));
        creditCard.setCustomer(user);
        payment.setAccount(creditCard);
        payment.setAmount(BigDecimal.valueOf(2000));

        when(merchantSettingRepository.findByMerchantId(1)).thenReturn(merchantSetting);
        when(paymentRepository.findById(2)).thenReturn(Optional.of(payment));
        when(refundRepository.save(any(Refund.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        doThrow(RestClientException.class).when(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));

        Refund refund=refundService.validated(1,2,BigDecimal.valueOf(20001));

        assertThat(refund.getRefundStatus()).isEqualTo(RefundStatus.FAILURE);
        assertThat(refund.getReasons().get(0)).isEqualTo("Merchant settings require partial payment but this payment is more than the required amount or is being made as a complete refund");

        verify(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));
        verify(paymentRepository).findById(2);
        verify(refundRepository).save(any(Refund.class));
        verify(merchantSettingRepository).findByMerchantId(1);
verify(webHookRetriesRepository).save(any(WebhookRetries.class));
    }

    @Test
    void settlementProcessorTest_Success(){
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(1000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");
        merchantSetting.setRefundType(RefundType.PARTIAL);

        MerchantAccount merchantAccount=new MerchantAccount();
        merchantAccount.setBalance(BigDecimal.valueOf(2000));

        Refund refund=new Refund();
        refund.setRefundStatus(RefundStatus.PROCESSING);
        refund.setAmount(BigDecimal.valueOf(200));


        User merchant=new User();
        merchant.setId(1);

        merchantSetting.setMerchant(merchant);

        Payment payment=new Payment();
        payment.setPaymentStatus(PaymentStatus.SETTLED);
        payment.setLocation(Location.Australia);
        payment.setId(1);
        payment.setAmountLeft(BigDecimal.valueOf(2000));
        payment.setMerchant(merchant);
refund.setPayment(payment);
        User user=new User();
        user.setId(1);

        CreditCard creditCard=new CreditCard();
        creditCard.setId(1);
        creditCard.setMoneyRemaining(BigDecimal.valueOf(5000));
        creditCard.setCustomer(user);
        payment.setAccount(creditCard);
        payment.setAmount(BigDecimal.valueOf(2000));

        when(merchantSettingRepository.findByMerchantId(1)).thenReturn(merchantSetting);
        when(merchantAccountRepository.save(merchantAccount)).thenReturn(merchantAccount);
        when(merchantAccountRepository.findByMerchantId(payment.getMerchant().getId())).thenReturn(merchantAccount);
        when(refundRepository.save(any(Refund.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        doNothing().when(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));
        when(accountRepository.save(creditCard)).thenReturn(creditCard);

        Refund refund1=refundService.settlementProcessor(refund);

        assertThat(refund.getRefundStatus()).isEqualTo(RefundStatus.SETTLEMENT);
        assertThat(merchantAccount.getBalance()).isEqualTo(BigDecimal.valueOf(1800));
        assertThat(creditCard.getMoneyRemaining()).isEqualTo(BigDecimal.valueOf(5200));
        assertThat(payment.getAmountLeft()).isEqualTo(BigDecimal.valueOf(1800));

        verify(merchantSettingRepository).findByMerchantId(1);
        verify(merchantAccountRepository).save(merchantAccount);
        verify(refundRepository).save(any(Refund.class));
        verify(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));
        verify(accountRepository).save(creditCard);
        verify(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));


    }

    @Test
    void settlementProcessorTest_WebhookNotSent(){
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(1000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");
        merchantSetting.setRefundType(RefundType.PARTIAL);

        MerchantAccount merchantAccount=new MerchantAccount();
        merchantAccount.setBalance(BigDecimal.valueOf(2000));

        Refund refund=new Refund();
        refund.setRefundStatus(RefundStatus.PROCESSING);
        refund.setAmount(BigDecimal.valueOf(200));


        User merchant=new User();
        merchant.setId(1);

        merchantSetting.setMerchant(merchant);

        Payment payment=new Payment();
        payment.setPaymentStatus(PaymentStatus.SETTLED);
        payment.setLocation(Location.Australia);
        payment.setId(1);
        payment.setAmountLeft(BigDecimal.valueOf(2000));
        payment.setMerchant(merchant);
        refund.setPayment(payment);
        User user=new User();
        user.setId(1);

        CreditCard creditCard=new CreditCard();
        creditCard.setId(1);
        creditCard.setMoneyRemaining(BigDecimal.valueOf(5000));
        creditCard.setCustomer(user);
        payment.setAccount(creditCard);
        payment.setAmount(BigDecimal.valueOf(2000));

        when(merchantSettingRepository.findByMerchantId(1)).thenReturn(merchantSetting);
        when(merchantAccountRepository.save(merchantAccount)).thenReturn(merchantAccount);
        when(merchantAccountRepository.findByMerchantId(payment.getMerchant().getId())).thenReturn(merchantAccount);
        when(refundRepository.save(any(Refund.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        doThrow(RestClientException.class).when(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));
        when(accountRepository.save(creditCard)).thenReturn(creditCard);

        Refund refund1=refundService.settlementProcessor(refund);

        assertThat(refund.getRefundStatus()).isEqualTo(RefundStatus.SETTLEMENT);
        assertThat(merchantAccount.getBalance()).isEqualTo(BigDecimal.valueOf(1800));
        assertThat(creditCard.getMoneyRemaining()).isEqualTo(BigDecimal.valueOf(5200));
        assertThat(payment.getAmountLeft()).isEqualTo(BigDecimal.valueOf(1800));

        verify(merchantSettingRepository).findByMerchantId(1);
        verify(merchantAccountRepository).save(merchantAccount);
        verify(refundRepository).save(any(Refund.class));
        verify(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));
        verify(accountRepository).save(creditCard);
        verify(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));
        verify(webHookRetriesRepository).save(any(WebhookRetries.class));

    }




}
