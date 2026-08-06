package com.jose.hospital.repositories;

import com.jose.hospital.domain.AppointmentAuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentAuditEventRepository extends JpaRepository<AppointmentAuditEvent, Long> {
    List<AppointmentAuditEvent> findByAppointment_IdOrderByCreatedAtAsc(Long appointmentId);
}
