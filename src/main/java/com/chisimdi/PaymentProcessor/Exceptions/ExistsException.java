package com.chisimdi.PaymentProcessor.Exceptions;

public class ExistsException extends RuntimeException{
    public ExistsException(String message){
        super(message);
    }
}
