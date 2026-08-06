package com.jose.hospital.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentCreateRequest {

    @NotNull(message = "patient id cannot be null")
    private Long patientId;
    @NotNull(message = "doctor id cannot be null")
    private Long doctorId;
    @NotNull(message = "start at cannot be null")
    private LocalDateTime startAt;
    @NotNull(message = "end at cannot be null")
    private LocalDateTime endAt;
}
