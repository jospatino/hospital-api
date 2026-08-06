package com.jose.hospital.mappers;

import com.jose.hospital.domain.Doctor;
import com.jose.hospital.dtos.DoctorResponse;
import org.springframework.stereotype.Component;

@Component
public class DoctorMapper {
    public DoctorResponse toResponse(Doctor doctor){
        return DoctorResponse.builder()
                .id(doctor.getId())
                .email(doctor.getEmail())
                .active(doctor.isActive())
                .name(doctor.getName())
                .registrationDate(doctor.getDateOfRegister())
                .specialty(doctor.getSpecialty())
                .build();
    }
}
