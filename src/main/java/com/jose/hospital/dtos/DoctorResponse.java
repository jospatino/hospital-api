package com.jose.hospital.dtos;

import com.jose.hospital.domain.Specialty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class DoctorResponse {
    private Long id;
    private String name;
    private String email;
    private Specialty specialty;
    private LocalDate registrationDate;
    private boolean active;
}
