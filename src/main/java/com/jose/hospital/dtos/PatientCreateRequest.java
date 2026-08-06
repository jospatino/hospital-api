package com.jose.hospital.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PatientCreateRequest {
    @NotBlank(message = "Name cannot be blank")
    @Size(max = 150, message = "name most be between 1 and 150")
    private String name;
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Not email format")
    @Size(max = 150, message = "name most be between 1 and 150")
    private String email;
    @NotNull(message = "Doctor's id cannot be null")
    private Long doctorId;
}
