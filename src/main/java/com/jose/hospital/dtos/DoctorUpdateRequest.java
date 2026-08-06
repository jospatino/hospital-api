package com.jose.hospital.dtos;

import com.jose.hospital.domain.Specialty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorUpdateRequest {
    @Size(min = 2, max = 150, message = "Name must be between 2 and 150 characters")
    private String name;
    @Email(message = "Invalid email format")
    @Size(max = 150, message = "Email must be between 2 and 150 characters")
    private String email;

    private Specialty specialty;
}
