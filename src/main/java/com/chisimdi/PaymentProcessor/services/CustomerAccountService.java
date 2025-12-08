package com.chisimdi.PaymentProcessor.services;

import com.chisimdi.PaymentProcessor.Exceptions.ResourceNotFoundException;
import com.chisimdi.PaymentProcessor.models.*;
import com.chisimdi.PaymentProcessor.repository.BankAccountRepository;
import com.chisimdi.PaymentProcessor.repository.CreditCardRepository;
import com.chisimdi.PaymentProcessor.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerAccountService {
    private static final Logger log = LoggerFactory.getLogger(CustomerAccountService.class);
    private BankAccountRepository bankAccountRepository;
    private CreditCardRepository creditCardRepository;
    private UserRepository userRepository;

    public CustomerAccountService(BankAccountRepository bankAccountRepository,CreditCardRepository creditCardRepository,UserRepository userRepository){
        this.bankAccountRepository=bankAccountRepository;
        this.creditCardRepository=creditCardRepository;
        this.userRepository=userRepository;
    }
    public BankAccountDTO toBankAccountDTO(BankAccount bankAccount){
        BankAccountDTO bankAccountDTO=new BankAccountDTO();
        if(bankAccount.getCurrency()!=null){
            bankAccountDTO.setCurrency(bankAccount.getCurrency());
        }
        if(bankAccount.getMoneyRemaining()!=null){
            bankAccountDTO.setBalance(bankAccount.getMoneyRemaining());
        }
        bankAccountDTO.setId(bankAccount.getId());
        return bankAccountDTO;
    }
    public CreditCardDTO toCreditCardDTO(CreditCard creditCard){
        CreditCardDTO creditCardDTO=new CreditCardDTO();
        if(creditCard.getCreditLimit()!=null){
            creditCardDTO.setCreditLimit(creditCard.getCreditLimit());
        }
        if(creditCard.getMoneyRemaining()!=null){
            creditCardDTO.setTotalCreditRemaining(creditCard.getMoneyRemaining());
        }
        if(creditCard.getCurrency()!=null){
            creditCardDTO.setCurrency(creditCard.getCurrency());

        }
        creditCardDTO.setId(creditCard.getId());
        return creditCardDTO;
    }
    @Transactional
    public CreditCardDTO createNewCreditCard(int userId,CreditCard creditCard){
        log.info("creating credit card with user id {}",userId);
        User user= userRepository.findByIdAndRole(userId,"Customer");
        if(user==null){
            throw new ResourceNotFoundException("User with id "+userId+" not found");
        }
        creditCard.setCustomer(user);
        creditCard.setMoneyRemaining(creditCard.getCreditLimit());
        return toCreditCardDTO(creditCardRepository.save(creditCard));
    }
@Transactional
    public BankAccountDTO createNewBankAccount(int userId, BankAccount bankAccount){
        log.info("Creating account with userId {}",userId);
        User user= userRepository.findByIdAndRole(userId,"Customer");
        if(user==null){
            throw new ResourceNotFoundException("User with id "+userId+" not found");
        }
        log.info("account created");
        bankAccount.setCustomer(user);
        return toBankAccountDTO(bankAccountRepository.save(bankAccount));
    }

    public List<BankAccountDTO> findAllBankAccounts(int pageNumber, int size){
        List<BankAccountDTO>bankAccountDTOS=new ArrayList<>();
        Page<BankAccount>bankAccounts=bankAccountRepository.findAll(PageRequest.of(pageNumber, size));
        for(BankAccount b:bankAccounts){
            bankAccountDTOS.add(toBankAccountDTO(b));
        }
        log.info("found all bank accounts");
        return bankAccountDTOS;

    }

    public List<CreditCardDTO> findAllCreditCards(int pageNumber, int size){
        List<CreditCardDTO> creditCardDTOS =new ArrayList<>();
        Page<CreditCard>creditCards=creditCardRepository.findAll(PageRequest.of(pageNumber, size));
        for(CreditCard b:creditCards){
            creditCardDTOS.add(toCreditCardDTO(b));
        }
        log.info("found all credit cards");
        return creditCardDTOS;

    }

    public List<CreditCardDTO>findCreditCardsByUsers(int pageNumber,int size,int customerId){
        List<CreditCardDTO> creditCardDTOS =new ArrayList<>();
        Page<CreditCard>creditCards=creditCardRepository.findByCustomerId(customerId,PageRequest.of(pageNumber, size));
        for(CreditCard b:creditCards){
            creditCardDTOS.add(toCreditCardDTO(b));
        }
        log.info("found users credit cards");
        return creditCardDTOS;
    }

    public List<BankAccountDTO>findBankAccountByUsers(int pageNumber,int size,int customerId){
            List<BankAccountDTO>bankAccountDTOS=new ArrayList<>();
            Page<BankAccount>bankAccounts=bankAccountRepository.findByCustomerId(customerId,PageRequest.of(pageNumber, size));
            for(BankAccount b:bankAccounts){
                bankAccountDTOS.add(toBankAccountDTO(b));
            }
            log.info("found users bank account");
            return bankAccountDTOS;
    }


}
