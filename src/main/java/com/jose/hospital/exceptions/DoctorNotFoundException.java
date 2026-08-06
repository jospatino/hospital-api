package com.jose.hospital.exceptions;

public class DoctorNotFoundException extends RuntimeException{
    public DoctorNotFoundException(Long id){
        super("Doctor with id: " + id + " is not found");
    }
}
