package com.jose.hospital.exceptions;

public class BusinessValidationException extends RuntimeException{
    public BusinessValidationException(String message) {
        super(message);
    }
}
