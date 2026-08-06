package com.jose.hospital.exceptions;

public class AppointmentOverlapException extends RuntimeException{

    public AppointmentOverlapException(String message){
        super(message);
    }
}
