package com.chisimdi.PaymentProcessor;

import com.chisimdi.PaymentProcessor.Exceptions.ResourceNotFoundException;
import com.chisimdi.PaymentProcessor.models.*;
import com.chisimdi.PaymentProcessor.repository.BankAccountRepository;
import com.chisimdi.PaymentProcessor.repository.CreditCardRepository;
import com.chisimdi.PaymentProcessor.repository.UserRepository;
import com.chisimdi.PaymentProcessor.services.CustomerAccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.webmvc.autoconfigure.WebMvcProperties;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerAccountTest {
    @Mock
    BankAccountRepository bankAccountRepository;
    @Mock
    CreditCardRepository creditCardRepository;
    @Mock
    UserRepository userRepository;
    @InjectMocks
    CustomerAccountService customerAccountService;

    @Test
    void createCreditCardTest(){
        User user=new User();
        CreditCard creditCard=new CreditCard();
        when(userRepository.findByIdAndRole(1,"Customer")).thenReturn(user);
        when(creditCardRepository.save(creditCard)).thenReturn(creditCard);

        CreditCardDTO creditCardDTO=customerAccountService.createNewCreditCard(1,creditCard);

        assertThat(creditCardDTO.getCreditLimit()).isEqualTo(creditCardDTO.getCreditLimit());
        assertThat(creditCardDTO.getCurrency()).isEqualTo(creditCard.getCurrency());
        assertThat(creditCardDTO.getTotalCreditRemaining()).isEqualTo(creditCard.getMoneyRemaining());
        assertThat(creditCard.getCustomer()).isEqualTo(user);

        verify(userRepository).findByIdAndRole(1,"Customer");
        verify(creditCardRepository).save(creditCard);
    }
    @Test
    void createCreditCardTest_ThrowsResourceNotFoundException(){
        User user=new User();
        CreditCard creditCard=new CreditCard();
        when(userRepository.findByIdAndRole(1,"Customer")).thenReturn(null);


        assertThatThrownBy(()->customerAccountService.createNewCreditCard(1,creditCard)).isInstanceOf(ResourceNotFoundException.class);



        verify(userRepository).findByIdAndRole(1,"Customer");
        verify(creditCardRepository,never()).save(creditCard);
    }
    @Test
    void createBankAccountTest(){
            User user=new User();
            BankAccount bankAccount=new BankAccount();
            when(userRepository.findByIdAndRole(1,"Customer")).thenReturn(user);
            when(bankAccountRepository.save(bankAccount)).thenReturn(bankAccount);

            BankAccountDTO bankAccountDTO =customerAccountService.createNewBankAccount(1,bankAccount);


            assertThat(bankAccountDTO.getCurrency()).isEqualTo(bankAccount.getCurrency());
            assertThat(bankAccountDTO.getBalance()).isEqualTo(bankAccount.getMoneyRemaining());
            assertThat(bankAccount.getCustomer()).isEqualTo(user);

            verify(userRepository).findByIdAndRole(1,"Customer");
            verify(bankAccountRepository).save(bankAccount);
        }

    @Test
    void createBankAccountTest_ThrowsResourceNotFoundException(){
        User user=new User();
        BankAccount bankAccount=new BankAccount();
        when(userRepository.findByIdAndRole(1,"Customer")).thenReturn(null);

assertThatThrownBy(()->customerAccountService.createNewBankAccount(1,bankAccount)).isInstanceOf(ResourceNotFoundException.class);


        verify(userRepository).findByIdAndRole(1,"Customer");
        verify(bankAccountRepository,never()).save(bankAccount);
    }
    }



