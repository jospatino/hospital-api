package com.jose.hospital.dtos;

import com.jose.hospital.domain.Specialty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorRequest {

    @NotBlank(message = "Name cannot be empty")
    @Size(min = 2, max = 150, message = "Name must be between 2 and 150 characters")
    private String name;

    @NotBlank(message = "Email cannot be blank")
    @Size(max = 150, message = "Email must have a maximum of 150 characters")
    @Email(message = "Invalid format")
    private String email;
    @NotNull(message = "Specialty is required")
    private Specialty specialty;
}
