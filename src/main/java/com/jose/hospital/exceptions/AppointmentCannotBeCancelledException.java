package com.jose.hospital.exceptions;

public class AppointmentCannotBeCancelledException extends RuntimeException{
    public AppointmentCannotBeCancelledException(String message) {
        super(message);
    }
}
