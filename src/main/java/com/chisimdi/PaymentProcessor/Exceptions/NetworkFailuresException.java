package com.chisimdi.PaymentProcessor.Exceptions;

public class NetworkFailuresException extends RuntimeException{
    public NetworkFailuresException(String message){
        super(message);
    }
}
