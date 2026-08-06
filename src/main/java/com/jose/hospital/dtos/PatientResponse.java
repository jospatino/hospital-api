package com.jose.hospital.dtos;

import java.time.LocalDate;

public record PatientResponse(
        Long id,
        String name,
        String email,
        LocalDate dateOfRegistry,
        boolean active,
        Long doctorId,
        String doctorName

) {}
