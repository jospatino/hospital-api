package com.jose.hospital.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentRescheduleRequest {

    @NotNull
    private LocalDateTime startAt;

    @NotNull
    private LocalDateTime endAt;


}
