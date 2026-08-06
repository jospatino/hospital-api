package com.jose.hospital.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientUpdateRequest {
    @Size(max = 150)
    private String name;
    @Size(max = 150)
    @Email
    private String email;
    private Long doctorId;

}
