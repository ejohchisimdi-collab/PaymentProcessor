package com.chisimdi.PaymentProcessor;

import com.chisimdi.PaymentProcessor.Exceptions.InvalidCredentialsException;
import com.chisimdi.PaymentProcessor.Exceptions.ResourceNotFoundException;
import com.chisimdi.PaymentProcessor.models.*;
import com.chisimdi.PaymentProcessor.repository.*;
import com.chisimdi.PaymentProcessor.services.PaymentService;
import com.chisimdi.PaymentProcessor.services.WebHookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.parameters.P;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private MerchantSettingRepository merchantSettingRepository;
    @Mock
    private MerchantAccountRepository merchantAccountRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private Random random;
    @Mock
    private PaymentIdempotencyRepository paymentIdempotencyRepository;
    @Mock
    private WebHookService webHookService;
    @Mock
    private WebHookRetriesRepository webHookRetriesRepository;
    @InjectMocks
    PaymentService paymentService;

    @Test
    void pendingProcessorTestBankSuccess() {
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(5000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");

        BankAccount bankAccount = new BankAccount();
        bankAccount.setCurrency(Currency.USD);

        WebhookEvents webhookEvents = new WebhookEvents();
        User merchant = new User();
        when(merchantSettingRepository.findByMerchantId(1)).thenReturn(merchantSetting);
        when(accountRepository.findById(2)).thenReturn(Optional.of(bankAccount));
        when(userRepository.findByIdAndRole(1, "Merchant")).thenReturn(merchant);
        doNothing().when(webHookService).sendWebhook(anyString(), any(WebhookEvents.class));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        Payment payment = paymentService.pendingProcessor(1, 2, BigDecimal.valueOf(2000), Location.Afghanistan);


        assertThat(payment.getAccountType()).isEqualTo(AccountType.BANK);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CAPTURED);

        verify(paymentRepository).save(any(Payment.class));
        verify(accountRepository).findById(2);
        verify(userRepository).findByIdAndRole(1, "Merchant");
        verify(merchantSettingRepository).findByMerchantId(1);
        verify(webHookService).sendWebhook(anyString(), any(WebhookEvents.class));

    }

    @Test
    void pendingProcessorTestCreditSuccess() {
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(5000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");

        CreditCard creditCard = new CreditCard();
        creditCard.setCurrency(Currency.USD);

        WebhookEvents webhookEvents = new WebhookEvents();
        User merchant = new User();
        when(merchantSettingRepository.findByMerchantId(1)).thenReturn(merchantSetting);
        when(accountRepository.findById(2)).thenReturn(Optional.of(creditCard));
        when(userRepository.findByIdAndRole(1, "Merchant")).thenReturn(merchant);
        doNothing().when(webHookService).sendWebhook(anyString(), any(WebhookEvents.class));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));


        Payment payment = paymentService.pendingProcessor(1, 2, BigDecimal.valueOf(2000), Location.Afghanistan);


        assertThat(payment.getAccountType()).isEqualTo(AccountType.CREDIT);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);

        verify(paymentRepository).save(any(Payment.class));
        verify(accountRepository).findById(2);
        verify(userRepository).findByIdAndRole(1, "Merchant");
        verify(merchantSettingRepository).findByMerchantId(1);
        verify(webHookService).sendWebhook(anyString(), any(WebhookEvents.class));

    }

    @Test
    void pendingProcessorTest_ThrowsResourceNotFoundExceptionForMerchantSetting() {
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(5000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");

        CreditCard creditCard = new CreditCard();
        creditCard.setCurrency(Currency.USD);

        WebhookEvents webhookEvents = new WebhookEvents();
        User merchant = new User();
        when(merchantSettingRepository.findByMerchantId(1)).thenReturn(null);




        assertThatThrownBy(() -> paymentService.pendingProcessor(1, 2, BigDecimal.valueOf(2000), Location.Afghanistan)).isInstanceOf(ResourceNotFoundException.class);


        verify(paymentRepository, never()).save(any(Payment.class));
        verify(accountRepository).findById(2);
        verify(userRepository,never()).findByIdAndRole(1, "Merchant");
        verify(merchantSettingRepository).findByMerchantId(1);
        verify(webHookService, never()).sendWebhook(anyString(), any(WebhookEvents.class));
    }

    @Test
    void pendingProcessorTest_ThrowsResourceNotFoundExceptionForAccount() {
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(5000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");

        CreditCard creditCard = new CreditCard();
        creditCard.setCurrency(Currency.USD);

        WebhookEvents webhookEvents = new WebhookEvents();
        User merchant = new User();
        when(merchantSettingRepository.findByMerchantId(1)).thenReturn(merchantSetting);
        when(accountRepository.findById(2)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> paymentService.pendingProcessor(1, 2, BigDecimal.valueOf(2000), Location.Afghanistan)).isInstanceOf(ResourceNotFoundException.class);


        verify(paymentRepository, never()).save(any(Payment.class));
        verify(accountRepository).findById(2);
        verify(userRepository,never()).findByIdAndRole(1, "Merchant");
        verify(merchantSettingRepository).findByMerchantId(1);
        verify(webHookService, never()).sendWebhook(anyString(), any(WebhookEvents.class));
    }
    @Test
    void pendingProcessorTest_ThrowsResourceNotFoundExceptionForMerchant() {
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(5000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");

        CreditCard creditCard = new CreditCard();
        creditCard.setCurrency(Currency.USD);

        WebhookEvents webhookEvents = new WebhookEvents();
        User merchant = new User();
        when(merchantSettingRepository.findByMerchantId(1)).thenReturn(merchantSetting);
        when(accountRepository.findById(2)).thenReturn(Optional.of(creditCard));
        when(userRepository.findByIdAndRole(1,"Merchant")).thenReturn(null);

        assertThatThrownBy(() -> paymentService.pendingProcessor(1, 2, BigDecimal.valueOf(2000), Location.Afghanistan)).isInstanceOf(ResourceNotFoundException.class);


        verify(paymentRepository, never()).save(any(Payment.class));
        verify(accountRepository).findById(2);
        verify(userRepository).findByIdAndRole(1, "Merchant");
        verify(merchantSettingRepository).findByMerchantId(1);
        verify(webHookService, never()).sendWebhook(anyString(), any(WebhookEvents.class));
    }
    @Test
    void pendingProcessorTest_ThrowsInvalidCredentialExceptionForCurrency() {
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(5000));
        merchantSetting.setCurrency(Currency.NGN);
        merchantSetting.setMerchantEndpoint("http://localhost");

        CreditCard creditCard = new CreditCard();
        creditCard.setCurrency(Currency.USD);

        WebhookEvents webhookEvents = new WebhookEvents();
        User merchant = new User();
        when(merchantSettingRepository.findByMerchantId(1)).thenReturn(merchantSetting);
        when(accountRepository.findById(2)).thenReturn(Optional.of(creditCard));
        when(userRepository.findByIdAndRole(1,"Merchant")).thenReturn(merchant);

        assertThatThrownBy(() -> paymentService.pendingProcessor(1, 2, BigDecimal.valueOf(2000), Location.Afghanistan)).isInstanceOf(InvalidCredentialsException.class);


        verify(paymentRepository, never()).save(any(Payment.class));
        verify(accountRepository).findById(2);
        verify(userRepository).findByIdAndRole(1, "Merchant");
        verify(merchantSettingRepository).findByMerchantId(1);
        verify(webHookService, never()).sendWebhook(anyString(), any(WebhookEvents.class));
    }
    @Test
    void pendingProcessorTest_ThrowsInvalidCredentialExceptionForMoneyLimit() {
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(1000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");

        CreditCard creditCard = new CreditCard();
        creditCard.setCurrency(Currency.USD);

        WebhookEvents webhookEvents = new WebhookEvents();
        User merchant = new User();
        when(merchantSettingRepository.findByMerchantId(1)).thenReturn(merchantSetting);
        when(accountRepository.findById(2)).thenReturn(Optional.of(creditCard));
        when(userRepository.findByIdAndRole(1,"Merchant")).thenReturn(merchant);

        assertThatThrownBy(() -> paymentService.pendingProcessor(1, 2, BigDecimal.valueOf(2000), Location.Afghanistan)).isInstanceOf(InvalidCredentialsException.class);


        verify(paymentRepository, never()).save(any(Payment.class));
        verify(accountRepository).findById(2);
        verify(userRepository).findByIdAndRole(1, "Merchant");
        verify(merchantSettingRepository).findByMerchantId(1);
        verify(webHookService, never()).sendWebhook(anyString(), any(WebhookEvents.class));
    }
    @Test
    void pendingProcessorTest_WebhookIsNeverSavedButRetryIsSaved() {
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(5000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");

        CreditCard creditCard = new CreditCard();
        creditCard.setCurrency(Currency.USD);

        WebhookEvents webhookEvents = new WebhookEvents();
        User merchant = new User();
        when(merchantSettingRepository.findByMerchantId(1)).thenReturn(merchantSetting);
        when(accountRepository.findById(2)).thenReturn(Optional.of(creditCard));
        when(userRepository.findByIdAndRole(1,"Merchant")).thenReturn(merchant);
doThrow(RestClientException.class).when(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));


Payment payment= paymentService.pendingProcessor(1, 2, BigDecimal.valueOf(2000), Location.Afghanistan);


        verify(paymentRepository).save(any(Payment.class));
        verify(accountRepository).findById(2);
        verify(userRepository).findByIdAndRole(1, "Merchant");
        verify(merchantSettingRepository).findByMerchantId(1);
        verify(webHookService).sendWebhook(anyString(), any(WebhookEvents.class));
        verify(webHookRetriesRepository).save(any(WebhookRetries.class));
    }

    @Test
    void validatedProcessorSuccessTest(){
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(1000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");


        User merchant=new User();
        merchant.setId(1);

        merchantSetting.setMerchant(merchant);

        Payment payment=new Payment();
        payment.setPaymentStatus(PaymentStatus.PENDING);
payment.setLocation(Location.Albania);
payment.setId(1);
payment.setMerchant(merchant);

User user=new User();
user.setId(1);

CreditCard creditCard=new CreditCard();
creditCard.setId(1);
creditCard.setMoneyRemaining(BigDecimal.valueOf(5000));
creditCard.setCustomer(user);
payment.setAccount(creditCard);
payment.setAmount(BigDecimal.valueOf(2000));

        Payment payment1=new Payment();
        Payment payment2=new Payment();
        Payment payment3=new Payment();
        payment1.setLocalDate(LocalDateTime.now().plusMinutes(3));
        List<Payment>paymentList=List.of(payment3,payment2,payment1);
        payment3.setLocalDate(LocalDateTime.now());
        payment1.setLocation(Location.Albania);
        Payment payment4=new Payment();
        payment4.setPaymentStatus(PaymentStatus.SETTLED);
        Payment payment5=new Payment();
        payment5.setPaymentStatus(PaymentStatus.SETTLED);
        Payment payment6=new Payment();
        payment6.setPaymentStatus(PaymentStatus.SETTLED);
        List<Payment>paymentsThatFailed=List.of(payment5,payment4,payment6);

        when(paymentRepository.findByAccountCustomerIdAndPaymentStatus(payment.getAccount().getCustomer().getId(),PaymentStatus.SETTLED)).thenReturn(paymentList);
        when(paymentRepository.findByAccountId(payment.getAccount().getId())).thenReturn(paymentsThatFailed);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(merchantSettingRepository.findByMerchantId(merchant.getId())).thenReturn(merchantSetting);
        doNothing().when(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));        Payment paymentA=paymentService.validatedProcessor(payment);
        assertThat(paymentA.getPaymentStatus()).isEqualTo(PaymentStatus.VALIDATED);
        assertThat(paymentA.getWarnings().size()).isEqualTo(0);

        verify(paymentRepository).findByAccountCustomerIdAndPaymentStatus(payment.getAccount().getCustomer().getId(),PaymentStatus.SETTLED);
        verify(paymentRepository).findByAccountId(payment.getAccount().getId());
        verify(paymentRepository).save(any(Payment.class));
        verify(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));
    }
    @Test
    void validatedProcessorSuccessDifferentLocationTest(){
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(1000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");


        User merchant=new User();
        merchant.setId(1);

        merchantSetting.setMerchant(merchant);

        Payment payment=new Payment();
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setLocation(Location.Nigeria);
        payment.setId(1);
        payment.setMerchant(merchant);

        User user=new User();
        user.setId(1);

        CreditCard creditCard=new CreditCard();
        creditCard.setId(1);
        creditCard.setMoneyRemaining(BigDecimal.valueOf(5000));
        creditCard.setCustomer(user);
        payment.setAccount(creditCard);
        payment.setAmount(BigDecimal.valueOf(2000));

        Payment payment1=new Payment();
        Payment payment2=new Payment();
        Payment payment3=new Payment();
        payment1.setLocalDate(LocalDateTime.now().plusMinutes(3));
        List<Payment>paymentList=List.of(payment3,payment2,payment1);
        payment3.setLocalDate(LocalDateTime.now());
        payment1.setLocation(Location.Albania);
        Payment payment4=new Payment();
        payment4.setPaymentStatus(PaymentStatus.SETTLED);
        Payment payment5=new Payment();
        payment5.setPaymentStatus(PaymentStatus.SETTLED);
        Payment payment6=new Payment();
        payment6.setPaymentStatus(PaymentStatus.SETTLED);
        List<Payment>paymentsThatFailed=List.of(payment5,payment4,payment6);

        when(paymentRepository.findByAccountCustomerIdAndPaymentStatus(payment.getAccount().getCustomer().getId(),PaymentStatus.SETTLED)).thenReturn(paymentList);
        when(paymentRepository.findByAccountId(payment.getAccount().getId())).thenReturn(paymentsThatFailed);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(merchantSettingRepository.findByMerchantId(merchant.getId())).thenReturn(merchantSetting);
        doNothing().when(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));        Payment paymentA=paymentService.validatedProcessor(payment);
        assertThat(paymentA.getPaymentStatus()).isEqualTo(PaymentStatus.VALIDATED);
        assertThat(paymentA.getWarnings().get(0)).isEqualTo("A payment from a different location was made");

        verify(paymentRepository).findByAccountCustomerIdAndPaymentStatus(payment.getAccount().getCustomer().getId(),PaymentStatus.SETTLED);
        verify(paymentRepository).findByAccountId(payment.getAccount().getId());
        verify(paymentRepository).save(any(Payment.class));
        verify(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));
    }
    @Test
    void validatedProcessorSuccessFailedInSuccessionTest(){
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(1000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");


        User merchant=new User();
        merchant.setId(1);

        merchantSetting.setMerchant(merchant);

        Payment payment=new Payment();
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setLocation(Location.Albania);
        payment.setId(1);
        payment.setMerchant(merchant);

        User user=new User();
        user.setId(1);

        CreditCard creditCard=new CreditCard();
        creditCard.setId(1);
        creditCard.setMoneyRemaining(BigDecimal.valueOf(5000));
        creditCard.setCustomer(user);
        payment.setAccount(creditCard);
        payment.setAmount(BigDecimal.valueOf(2000));

        Payment payment1=new Payment();
        Payment payment2=new Payment();
        Payment payment3=new Payment();
        payment1.setLocalDate(LocalDateTime.now().plusMinutes(3));
        List<Payment>paymentList=List.of(payment3,payment2,payment1);
        payment3.setLocalDate(LocalDateTime.now());
        payment1.setLocation(Location.Albania);
        Payment payment4=new Payment();
        payment4.setPaymentStatus(PaymentStatus.FAILED);
        Payment payment5=new Payment();
        payment5.setPaymentStatus(PaymentStatus.FAILED);
        Payment payment6=new Payment();
        payment6.setPaymentStatus(PaymentStatus.FAILED);
        List<Payment>paymentsThatFailed=List.of(payment5,payment4,payment6);

        when(paymentRepository.findByAccountCustomerIdAndPaymentStatus(payment.getAccount().getCustomer().getId(),PaymentStatus.SETTLED)).thenReturn(paymentList);
        when(paymentRepository.findByAccountId(payment.getAccount().getId())).thenReturn(paymentsThatFailed);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(merchantSettingRepository.findByMerchantId(merchant.getId())).thenReturn(merchantSetting);
        doNothing().when(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));        Payment paymentA=paymentService.validatedProcessor(payment);
        assertThat(paymentA.getPaymentStatus()).isEqualTo(PaymentStatus.VALIDATED);
        assertThat(paymentA.getWarnings().get(0)).isEqualTo("Three consecutive failed payments were previously made");

        verify(paymentRepository).findByAccountCustomerIdAndPaymentStatus(payment.getAccount().getCustomer().getId(),PaymentStatus.SETTLED);
        verify(paymentRepository).findByAccountId(payment.getAccount().getId());
        verify(paymentRepository).save(any(Payment.class));
        verify(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));

    }
    @Test
    void validatedProcessorThreeConsecutivePaymentsMadeInAMinute(){
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(1000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");


        User merchant=new User();
        merchant.setId(1);

        merchantSetting.setMerchant(merchant);

        Payment payment=new Payment();
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setLocation(Location.Albania);
        payment.setId(1);
        payment.setMerchant(merchant);

        User user=new User();
        user.setId(1);

        CreditCard creditCard=new CreditCard();
        creditCard.setId(1);
        creditCard.setMoneyRemaining(BigDecimal.valueOf(5000));
        creditCard.setCustomer(user);
        payment.setAccount(creditCard);
        payment.setAmount(BigDecimal.valueOf(2000));

        Payment payment1=new Payment();
        Payment payment2=new Payment();
        Payment payment3=new Payment();
        payment1.setLocalDate(LocalDateTime.now().plusMinutes(0));
        List<Payment>paymentList=List.of(payment3,payment2,payment1);
        payment3.setLocalDate(LocalDateTime.now());
        payment1.setLocation(Location.Albania);
        Payment payment4=new Payment();
        payment4.setPaymentStatus(PaymentStatus.SETTLED);
        Payment payment5=new Payment();
        payment5.setPaymentStatus(PaymentStatus.SETTLED);
        Payment payment6=new Payment();
        payment6.setPaymentStatus(PaymentStatus.SETTLED);
        List<Payment>paymentsThatFailed=List.of(payment5,payment4,payment6);

        when(paymentRepository.findByAccountCustomerIdAndPaymentStatus(payment.getAccount().getCustomer().getId(),PaymentStatus.SETTLED)).thenReturn(paymentList);
        when(paymentRepository.findByAccountId(payment.getAccount().getId())).thenReturn(paymentsThatFailed);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(merchantSettingRepository.findByMerchantId(merchant.getId())).thenReturn(merchantSetting);
        doNothing().when(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));        Payment paymentA=paymentService.validatedProcessor(payment);
        assertThat(paymentA.getPaymentStatus()).isEqualTo(PaymentStatus.VALIDATED);
        assertThat(paymentA.getWarnings().get(0)).isEqualTo(" Three consecutive payments were made in less than one minute");

        verify(paymentRepository).findByAccountCustomerIdAndPaymentStatus(payment.getAccount().getCustomer().getId(),PaymentStatus.SETTLED);
        verify(paymentRepository).findByAccountId(payment.getAccount().getId());
        verify(paymentRepository).save(any(Payment.class));
        verify(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));

    }
    @Test
    void ValidatorProcessorTestInsufficientFunds(){
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(1000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");


        User merchant=new User();
        merchant.setId(1);

        merchantSetting.setMerchant(merchant);

        Payment payment=new Payment();
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setLocation(Location.Albania);
        payment.setId(1);
        payment.setMerchant(merchant);

        User user=new User();
        user.setId(1);

        CreditCard creditCard=new CreditCard();
        creditCard.setId(1);
        creditCard.setMoneyRemaining(BigDecimal.valueOf(1000));
        creditCard.setCustomer(user);
        payment.setAccount(creditCard);
        payment.setAmount(BigDecimal.valueOf(2000));

        Payment payment1=new Payment();
        Payment payment2=new Payment();
        Payment payment3=new Payment();
        payment1.setLocalDate(LocalDateTime.now().plusMinutes(3));
        List<Payment>paymentList=List.of(payment3,payment2,payment1);
        payment3.setLocalDate(LocalDateTime.now());
        payment1.setLocation(Location.Albania);
        Payment payment4=new Payment();
        payment4.setPaymentStatus(PaymentStatus.SETTLED);
        Payment payment5=new Payment();
        payment5.setPaymentStatus(PaymentStatus.SETTLED);
        Payment payment6=new Payment();
        payment6.setPaymentStatus(PaymentStatus.SETTLED);
        List<Payment>paymentsThatFailed=List.of(payment5,payment4,payment6);

        when(paymentRepository.findByAccountCustomerIdAndPaymentStatus(payment.getAccount().getCustomer().getId(),PaymentStatus.SETTLED)).thenReturn(paymentList);
        when(paymentRepository.findByAccountId(payment.getAccount().getId())).thenReturn(paymentsThatFailed);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(merchantSettingRepository.findByMerchantId(merchant.getId())).thenReturn(merchantSetting);
        doNothing().when(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));        Payment paymentA=paymentService.validatedProcessor(payment);
        assertThat(paymentA.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(paymentA.getWarnings().get(0)).isEqualTo("Insufficient funds");

        verify(paymentRepository).findByAccountCustomerIdAndPaymentStatus(payment.getAccount().getCustomer().getId(),PaymentStatus.SETTLED);
        verify(paymentRepository).findByAccountId(payment.getAccount().getId());
        verify(paymentRepository).save(any(Payment.class));
        verify(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));

    }
@Test
    void ValidatorProcessorTestThreeFraudScoreEqualsFailure(){
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(1000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");


        User merchant=new User();
        merchant.setId(1);

        merchantSetting.setMerchant(merchant);

        Payment payment=new Payment();
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setLocation(Location.Australia);
        payment.setId(1);
        payment.setMerchant(merchant);

        User user=new User();
        user.setId(1);

        CreditCard creditCard=new CreditCard();
        creditCard.setId(1);
        creditCard.setMoneyRemaining(BigDecimal.valueOf(5000));
        creditCard.setCustomer(user);
        payment.setAccount(creditCard);
        payment.setAmount(BigDecimal.valueOf(2000));

        Payment payment1=new Payment();
        Payment payment2=new Payment();
        Payment payment3=new Payment();
        payment1.setLocalDate(LocalDateTime.now().plusMinutes(0));
        List<Payment>paymentList=List.of(payment3,payment2,payment1);
        payment3.setLocalDate(LocalDateTime.now());
        payment1.setLocation(Location.Albania);
        Payment payment4=new Payment();
        payment4.setPaymentStatus(PaymentStatus.FAILED);
        Payment payment5=new Payment();
        payment5.setPaymentStatus(PaymentStatus.FAILED);
        Payment payment6=new Payment();
        payment6.setPaymentStatus(PaymentStatus.FAILED);
        List<Payment>paymentsThatFailed=List.of(payment5,payment4,payment6);

        when(paymentRepository.findByAccountCustomerIdAndPaymentStatus(payment.getAccount().getCustomer().getId(),PaymentStatus.SETTLED)).thenReturn(paymentList);
        when(paymentRepository.findByAccountId(payment.getAccount().getId())).thenReturn(paymentsThatFailed);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(merchantSettingRepository.findByMerchantId(merchant.getId())).thenReturn(merchantSetting);
        doNothing().when(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));        Payment paymentA=paymentService.validatedProcessor(payment);
        assertThat(paymentA.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(paymentA.getWarnings().size()).isEqualTo(3);

        verify(paymentRepository).findByAccountCustomerIdAndPaymentStatus(payment.getAccount().getCustomer().getId(),PaymentStatus.SETTLED);
        verify(paymentRepository).findByAccountId(payment.getAccount().getId());
        verify(paymentRepository).save(any(Payment.class));
        verify(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));

    }

    @Test
    void webhookNotSentValidatorTest(){
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(1000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");


        User merchant=new User();
        merchant.setId(1);

        merchantSetting.setMerchant(merchant);

        Payment payment=new Payment();
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setLocation(Location.Australia);
        payment.setId(1);
        payment.setMerchant(merchant);

        User user=new User();
        user.setId(1);

        CreditCard creditCard=new CreditCard();
        creditCard.setId(1);
        creditCard.setMoneyRemaining(BigDecimal.valueOf(5000));
        creditCard.setCustomer(user);
        payment.setAccount(creditCard);
        payment.setAmount(BigDecimal.valueOf(2000));

        Payment payment1=new Payment();
        Payment payment2=new Payment();
        Payment payment3=new Payment();
        payment1.setLocalDate(LocalDateTime.now().plusMinutes(0));
        List<Payment>paymentList=List.of(payment3,payment2,payment1);
        payment3.setLocalDate(LocalDateTime.now());
        payment1.setLocation(Location.Albania);
        Payment payment4=new Payment();
        payment4.setPaymentStatus(PaymentStatus.FAILED);
        Payment payment5=new Payment();
        payment5.setPaymentStatus(PaymentStatus.FAILED);
        Payment payment6=new Payment();
        payment6.setPaymentStatus(PaymentStatus.FAILED);
        List<Payment>paymentsThatFailed=List.of(payment5,payment4,payment6);

        when(paymentRepository.findByAccountCustomerIdAndPaymentStatus(payment.getAccount().getCustomer().getId(),PaymentStatus.SETTLED)).thenReturn(paymentList);
        when(paymentRepository.findByAccountId(payment.getAccount().getId())).thenReturn(paymentsThatFailed);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(merchantSettingRepository.findByMerchantId(merchant.getId())).thenReturn(merchantSetting);
        doThrow(RestClientException.class).when(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));        Payment paymentA=paymentService.validatedProcessor(payment);
        assertThat(paymentA.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(paymentA.getWarnings().size()).isEqualTo(3);

        verify(paymentRepository).findByAccountCustomerIdAndPaymentStatus(payment.getAccount().getCustomer().getId(),PaymentStatus.SETTLED);
        verify(paymentRepository).findByAccountId(payment.getAccount().getId());
        verify(paymentRepository).save(any(Payment.class));
        verify(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));
        verify(webHookRetriesRepository).save(any(WebhookRetries.class));
    }

    @Test
    void authorizationProcessor(){
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(1000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");


        User merchant=new User();
        merchant.setId(1);

        merchantSetting.setMerchant(merchant);

        Payment payment=new Payment();
        payment.setPaymentStatus(PaymentStatus.VALIDATED);
        payment.setLocation(Location.Australia);
        payment.setId(1);
        payment.setMerchant(merchant);

        User user=new User();
        user.setId(1);

        CreditCard creditCard=new CreditCard();
        creditCard.setId(1);
        creditCard.setMoneyRemaining(BigDecimal.valueOf(5000));
        creditCard.setCustomer(user);
        payment.setAccount(creditCard);
        payment.setAmount(BigDecimal.valueOf(2000));

        when(accountRepository.save(creditCard)).thenReturn(creditCard);
        when(merchantSettingRepository.findByMerchantId(merchant.getId())).thenReturn(merchantSetting);
        when(paymentRepository.save(payment)).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        doNothing().when(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));

        Payment payment1=paymentService.authorizationProcessor(payment);

        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
        assertThat(creditCard.getMoneyRemaining()).isEqualTo(BigDecimal.valueOf(3000));


        verify(accountRepository).save(creditCard);
        verify(merchantSettingRepository).findByMerchantId(merchant.getId());
        verify(paymentRepository).save(payment);
        verify(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));
    }

    @Test
    void authorizationProcessorWebhookNotSent(){
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(1000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");


        User merchant=new User();
        merchant.setId(1);

        merchantSetting.setMerchant(merchant);

        Payment payment=new Payment();
        payment.setPaymentStatus(PaymentStatus.VALIDATED);
        payment.setLocation(Location.Australia);
        payment.setId(1);
        payment.setMerchant(merchant);

        User user=new User();
        user.setId(1);

        CreditCard creditCard=new CreditCard();
        creditCard.setId(1);
        creditCard.setMoneyRemaining(BigDecimal.valueOf(5000));
        creditCard.setCustomer(user);
        payment.setAccount(creditCard);
        payment.setAmount(BigDecimal.valueOf(2000));

        when(accountRepository.save(creditCard)).thenReturn(creditCard);
        when(merchantSettingRepository.findByMerchantId(merchant.getId())).thenReturn(merchantSetting);
        when(paymentRepository.save(payment)).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        doThrow(RestClientException.class).when(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));

        Payment payment1=paymentService.authorizationProcessor(payment);

        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
        assertThat(creditCard.getMoneyRemaining()).isEqualTo(BigDecimal.valueOf(3000));


        verify(accountRepository).save(creditCard);
        verify(merchantSettingRepository).findByMerchantId(merchant.getId());
        verify(paymentRepository).save(payment);
        verify(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));
    verify(webHookRetriesRepository).save(any(WebhookRetries.class));
    }

    @Test
    void capturingProcessor(){
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(1000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");


        User merchant=new User();
        merchant.setId(1);

        merchantSetting.setMerchant(merchant);

        Payment payment=new Payment();
        payment.setPaymentStatus(PaymentStatus.AUTHORIZED);
        payment.setLocation(Location.Australia);
        payment.setId(1);
        payment.setMerchant(merchant);

        User user=new User();
        user.setId(1);

        CreditCard creditCard=new CreditCard();
        creditCard.setId(1);
        creditCard.setMoneyRemaining(BigDecimal.valueOf(5000));
        creditCard.setCustomer(user);
        payment.setAccount(creditCard);
        payment.setAmount(BigDecimal.valueOf(2000));


        when(merchantSettingRepository.findByMerchantId(merchant.getId())).thenReturn(merchantSetting);
        when(paymentRepository.save(payment)).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        doNothing().when(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));

        Payment payment1=paymentService.capturingProcessor(payment);

        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.CAPTURED);

        verify(paymentRepository).save(payment);
        verify(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));
    }

    @Test
    void settledProcessor(){
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(1000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");

        MerchantAccount merchantAccount=new MerchantAccount();
        merchantAccount.setBalance(BigDecimal.valueOf(2000));

        User merchant=new User();
        merchant.setId(1);

        merchantSetting.setMerchant(merchant);

        Payment payment=new Payment();
        payment.setPaymentStatus(PaymentStatus.CAPTURED);
        payment.setLocation(Location.Australia);
        payment.setId(1);
        payment.setMerchant(merchant);

        User user=new User();
        user.setId(1);

        CreditCard creditCard=new CreditCard();
        creditCard.setId(1);
        creditCard.setMoneyRemaining(BigDecimal.valueOf(5000));
        creditCard.setCustomer(user);
        payment.setAccount(creditCard);
        payment.setAmount(BigDecimal.valueOf(2000));

        when(merchantAccountRepository.findByMerchantId(merchant.getId())).thenReturn(merchantAccount);
        when(merchantSettingRepository.findByMerchantId(merchant.getId())).thenReturn(merchantSetting);
        doNothing().when(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));
        when(paymentRepository.save(payment)).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));


        Payment payment1=paymentService.settledProcessor(payment);

        assertThat(merchantAccount.getBalance()).isEqualTo(BigDecimal.valueOf(4000));
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.SETTLED);

        verify(merchantAccountRepository).findByMerchantId(merchant.getId());
        verify(merchantSettingRepository).findByMerchantId(merchant.getId());
        verify(paymentRepository).save(payment);
        verify(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));
    }

    @Test
    void settledProcessor_WebhookNotSent(){
        MerchantSetting merchantSetting = new MerchantSetting();
        merchantSetting.setMoneyLimit(new BigDecimal(1000));
        merchantSetting.setCurrency(Currency.USD);
        merchantSetting.setMerchantEndpoint("http://localhost");

        MerchantAccount merchantAccount=new MerchantAccount();
        merchantAccount.setBalance(BigDecimal.valueOf(2000));

        User merchant=new User();
        merchant.setId(1);

        merchantSetting.setMerchant(merchant);

        Payment payment=new Payment();
        payment.setPaymentStatus(PaymentStatus.CAPTURED);
        payment.setLocation(Location.Australia);
        payment.setId(1);
        payment.setMerchant(merchant);

        User user=new User();
        user.setId(1);

        CreditCard creditCard=new CreditCard();
        creditCard.setId(1);
        creditCard.setMoneyRemaining(BigDecimal.valueOf(5000));
        creditCard.setCustomer(user);
        payment.setAccount(creditCard);
        payment.setAmount(BigDecimal.valueOf(2000));

        when(merchantAccountRepository.findByMerchantId(merchant.getId())).thenReturn(merchantAccount);
        when(merchantSettingRepository.findByMerchantId(merchant.getId())).thenReturn(merchantSetting);
        doThrow(RestClientException.class).when(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));
        when(paymentRepository.save(payment)).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));


        Payment payment1=paymentService.settledProcessor(payment);

        assertThat(merchantAccount.getBalance()).isEqualTo(BigDecimal.valueOf(4000));
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.SETTLED);

        verify(merchantAccountRepository).findByMerchantId(merchant.getId());
        verify(merchantSettingRepository).findByMerchantId(merchant.getId());
        verify(paymentRepository).save(payment);
        verify(webHookService).sendWebhook(anyString(),any(WebhookEvents.class));
        verify(webHookRetriesRepository).save(any(WebhookRetries.class));
    }



}
