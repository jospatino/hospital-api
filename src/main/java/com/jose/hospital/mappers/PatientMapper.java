package com.jose.hospital.mappers;

import com.jose.hospital.domain.Patient;
import com.jose.hospital.dtos.PatientResponse;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {
    public PatientResponse toResponse(Patient patient){
        var doctor = patient.getDoctor();
        return new PatientResponse(
                patient.getId(),
                patient.getName(),
                patient.getEmail(),
                patient.getDateOfRegistry(),
                patient.isActive(),
                doctor != null ? doctor.getId() : null,
                doctor != null ? doctor.getName() : null
        );
    }
}

