package com.chisimdi.PaymentProcessor.services;

import com.chisimdi.PaymentProcessor.Exceptions.ResourceNotFoundException;
import com.chisimdi.PaymentProcessor.models.*;
import com.chisimdi.PaymentProcessor.repository.MerchantAccountRepository;
import com.chisimdi.PaymentProcessor.repository.MerchantSettingRepository;
import com.chisimdi.PaymentProcessor.repository.UserRepository;
import com.chisimdi.PaymentProcessor.utils.NewMerchantSettingUtil;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
@Service
public class MerchantService {
    private static final Logger log = LoggerFactory.getLogger(MerchantService.class);
    private MerchantSettingRepository merchantSettingRepository;
    private MerchantAccountRepository merchantAccountRepository;
    private UserRepository userRepository;

    public MerchantService(MerchantAccountRepository merchantAccountRepository, MerchantSettingRepository merchantSettingRepository, UserRepository userRepository) {
        this.merchantAccountRepository = merchantAccountRepository;
        this.merchantSettingRepository = merchantSettingRepository;
        this.userRepository = userRepository;
    }

    public MerchantAccountDTO toMerchantAccountDTO(MerchantAccount merchantAccount) {
        MerchantAccountDTO merchantAccountDTO = new MerchantAccountDTO();
        if (merchantAccount.getBalance() != null) {
            merchantAccountDTO.setBalance(merchantAccount.getBalance());
        }
        merchantAccountDTO.setId(merchantAccount.getId());
        return merchantAccountDTO;
    }

    public MerchantSettingDTO toMerchantSettingDTO(MerchantSetting merchantSetting) {
        MerchantSettingDTO merchantSettingDTO = new MerchantSettingDTO();
        if (merchantSetting.getCurrency() != null) {
            merchantSettingDTO.setCurrency(merchantSetting.getCurrency());
        }
        if (merchantSetting.getMoneyLimit() != null) {
            merchantSettingDTO.setMoneyLimit(merchantSetting.getMoneyLimit());
        }
        if(merchantSetting.getMerchantEndpoint()!=null){
            merchantSettingDTO.setMerchantEndpoint(merchantSetting.getMerchantEndpoint());
        }
        if(merchantSetting.getRefundType()!=null){
            merchantSettingDTO.setRefundType(merchantSetting.getRefundType());
        }
        merchantSettingDTO.setId(merchantSetting.getId());
        return merchantSettingDTO;
    }
@Transactional
    public MerchantSettingDTO createMerchantSetting(int merchantId, MerchantSetting merchantSetting) {

        log.info("Creating merchant settings for merchant with id {}",merchantId);
        User user = userRepository.findByIdAndRole(merchantId, "Merchant");
        if (user == null) {
            throw new ResourceNotFoundException("Merchant with Id " + merchantId + " not found");
        }
        merchantSetting.setMerchant(user);
        merchantSettingRepository.save(merchantSetting);
        log.info("Merchant setting created");
        return toMerchantSettingDTO(merchantSetting);
    }
@Transactional
    public MerchantSettingDTO updateMerchantSetting(int merchantId, NewMerchantSettingUtil newMerchantSetting) {
        log.info("updating merchant setting with merchant id {}",merchantId);
        MerchantSetting oldMerchantSetting = merchantSettingRepository.findByMerchantId(merchantId);
        if(oldMerchantSetting==null){
            throw new ResourceNotFoundException("merchant setting with merchant id "+merchantId+" not found");

        }
        if(newMerchantSetting.getMerchantEndpoint()!=null){
            oldMerchantSetting.setMerchantEndpoint(newMerchantSetting.getMerchantEndpoint());
        }
        if (newMerchantSetting.getCurrency()!=null){
            oldMerchantSetting.setCurrency(newMerchantSetting.getCurrency());

        }
        if(newMerchantSetting.getMoneyLimit()!=null){
            oldMerchantSetting.setMoneyLimit(newMerchantSetting.getMoneyLimit());

        }
        if(newMerchantSetting.getRefundType()!=null){
            oldMerchantSetting.setRefundType(newMerchantSetting.getRefundType());
        }
log.info("merchant setting updated sucessfully");
        return toMerchantSettingDTO(merchantSettingRepository.save(oldMerchantSetting));
    }
    public MerchantSettingDTO findMerchantSettingByMerchantId(int merchantId){
        MerchantSetting merchantSetting= merchantSettingRepository.findByMerchantId(merchantId);
        if(merchantSetting==null){
            throw new ResourceNotFoundException("merchant setting with merchant Id "+merchantId+" not found");
        }
        return toMerchantSettingDTO(merchantSetting);
    }
    public MerchantAccountDTO createMerchantAccount(int merchantId,MerchantAccount merchantAccount){
        User user= userRepository.findByIdAndRole(merchantId,"Merchant");
        if(user==null){
            throw new ResourceNotFoundException("merchant with id "+merchantId+" not found");
        }
        merchantAccount.setMerchant(user);
       return toMerchantAccountDTO( merchantAccountRepository.save(merchantAccount));
    }
    public MerchantAccountDTO findMerchantAccountByMerchantId(int merchantId){
        MerchantAccount merchantAccount= merchantAccountRepository.findByMerchantId(merchantId);
        if(merchantAccount==null){
            throw new ResourceNotFoundException("Merchant with Id "+merchantId+" not found");

        }
        return toMerchantAccountDTO(merchantAccount);

    }
    public List<MerchantAccountDTO>findAllMerchantAccounts(int pageNumber,int size){

        List<MerchantAccountDTO>merchantAccountDTOS=new ArrayList<>();
        Page<MerchantAccount> merchantAccounts=merchantAccountRepository.findAll(PageRequest.of(pageNumber, size));
        for(MerchantAccount m:merchantAccounts){
            merchantAccountDTOS.add(toMerchantAccountDTO(m));
        }
        return merchantAccountDTOS;
    }

}
