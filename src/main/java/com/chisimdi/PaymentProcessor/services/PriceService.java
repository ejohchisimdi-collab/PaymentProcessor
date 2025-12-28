package com.chisimdi.PaymentProcessor.services;

import com.chisimdi.PaymentProcessor.Exceptions.ResourceNotFoundException;
import com.chisimdi.PaymentProcessor.models.PaymentInterval;
import com.chisimdi.PaymentProcessor.models.PriceDTO;
import com.chisimdi.PaymentProcessor.models.Prices;
import com.chisimdi.PaymentProcessor.models.User;
import com.chisimdi.PaymentProcessor.repository.PriceRepository;
import com.chisimdi.PaymentProcessor.repository.UserRepository;
import jakarta.persistence.Column;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PriceService {
    private PriceRepository priceRepository;
    private UserRepository userRepository;
    public PriceService(PriceRepository priceRepository,UserRepository userRepository){
        this.priceRepository=priceRepository;
        this.userRepository=userRepository;
    }

    public PriceDTO toPriceDTO(Prices price){
        PriceDTO priceDTO=new PriceDTO();
        priceDTO.setId(price.getId());
        if(price.getAmount()!=null){
            priceDTO.setAmount(price.getAmount());
        }
        if(price.getMerchant()!=null){
            priceDTO.setMerchantId(price.getMerchant().getId());
        }
        if(price.getPaymentInterval()!=null){
            priceDTO.setInterval(price.getPaymentInterval());
        }
        priceDTO.setIntervalCount(price.getIntervalCount());
        if(price.getProductName()!=null){
            priceDTO.setProductName(price.getProductName());
        }
        priceDTO.setRetryAttempts(price.getRetryAttempts());
        return priceDTO;

    }
    public PriceDTO createPrice(BigDecimal amount,String productName,int intervalCount,PaymentInterval paymentInterval,int retryAttempts,int merchantId){
        User merchant= userRepository.findByIdAndRole(merchantId,"Merchant");
        if(merchant==null){
            throw new ResourceNotFoundException("Merchant with id "+merchantId+" not found" );
        }
        Prices prices=new Prices();
        prices.setMerchant(merchant);
        prices.setProductName(productName);
        prices.setPaymentInterval(paymentInterval);
        prices.setIntervalCount(intervalCount);
        prices.setAmount(amount);
        prices.setRetryAttempts(retryAttempts);
        priceRepository.save(prices);
        return toPriceDTO(prices);

    }
    public List<PriceDTO> findPricesByMerchantId(int merchantId,int pageNumber,int size){
        Page<Prices> prices=priceRepository.findByMerchantId(merchantId, PageRequest.of(pageNumber,size));
        List<PriceDTO>priceDTOS=new ArrayList<>();
        for(Prices p:prices){
            priceDTOS.add(toPriceDTO(p));
        }
        return priceDTOS;

    }
    public List<PriceDTO> findAllPrices(int pageNumber,int size){
        Page<Prices> prices=priceRepository.findAll( PageRequest.of(pageNumber,size));
        List<PriceDTO>priceDTOS=new ArrayList<>();
        for(Prices p:prices){
            priceDTOS.add(toPriceDTO(p));
        }
        return priceDTOS;

    }
}
