package com.jose.hospital.exceptions;

public class InvalidAppointmentTimeException extends RuntimeException{

    public InvalidAppointmentTimeException(String message){
        super(message);
    }
}
