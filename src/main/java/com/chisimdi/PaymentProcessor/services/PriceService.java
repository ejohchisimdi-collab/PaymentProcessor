package com.chisimdi.PaymentProcessor.services;

import com.chisimdi.PaymentProcessor.models.Price;
import com.chisimdi.PaymentProcessor.models.PriceDTO;
import com.chisimdi.PaymentProcessor.repository.PriceRepository;
import org.springframework.stereotype.Service;

@Service
public class PriceService {
    public PriceRepository priceRepository;

    public PriceService(PriceRepository priceRepository){
        this.priceRepository=priceRepository;
    }

    public PriceDTO toPriceDTO(Price price){

    }
}
