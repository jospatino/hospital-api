package com.jose.hospital.dtos;

import com.jose.hospital.domain.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentResponse {

    private Long id;
    private Long doctorId;
    private Long patientId;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private AppointmentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;
}
