package com.jose.hospital.exceptions;

public class NullDoctorException extends RuntimeException{
    public NullDoctorException() {
        super("Doctor cannot be null");
    }
}
