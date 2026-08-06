package com.jose.hospital.exceptions;

public class InvalidAppointmentStatusException extends RuntimeException{
    public InvalidAppointmentStatusException(String message) {
        super(message);
    }
}
