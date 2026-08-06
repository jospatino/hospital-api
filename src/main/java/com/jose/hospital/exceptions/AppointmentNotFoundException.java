package com.jose.hospital.exceptions;

public class AppointmentNotFoundException extends RuntimeException{
    public AppointmentNotFoundException(Long id) {
        super("Appointment with id " + id + " was not found");
    }
}
