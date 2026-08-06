package com.jose.hospital.mappers;

import com.jose.hospital.domain.Appointment;
import com.jose.hospital.dtos.AppointmentResponse;

public class AppointmentMapper {

    public static AppointmentResponse toResponse(Appointment appointment){
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getDoctor().getId(),
                appointment.getPatient().getId(),
                appointment.getStartAt(),
                appointment.getEndAt(),
                appointment.getStatus(),
                appointment.getCreatedAt(),
                appointment.getCancelledAt()
        );
    }
}
