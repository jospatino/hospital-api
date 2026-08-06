package com.jose.hospital.services;

import com.jose.hospital.domain.AppointmentStatus;
import com.jose.hospital.dtos.AppointmentCreateRequest;
import com.jose.hospital.dtos.AppointmentRescheduleRequest;
import com.jose.hospital.dtos.AppointmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

public interface AppointmentService {

    public AppointmentResponse create(AppointmentCreateRequest request);
    public AppointmentResponse cancel(Long id);
    Page<AppointmentResponse> getDoctorAppointments(Long doctorId, LocalDateTime from,
                                            LocalDateTime to, AppointmentStatus status,
                                            Pageable pageable);
    Page<AppointmentResponse> getPatientAppointments(
            Long patientId,
            LocalDateTime from,
            LocalDateTime to,
            AppointmentStatus status,
            Pageable pageable
    );

    AppointmentResponse completeAppointment(Long id);

    AppointmentResponse reschedule(Long id, AppointmentRescheduleRequest request);
}
