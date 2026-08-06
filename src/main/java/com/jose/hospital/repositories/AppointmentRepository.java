package com.jose.hospital.repositories;

import com.jose.hospital.domain.Appointment;
import com.jose.hospital.domain.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    @Query("""
           SELECT (COUNT(a) > 0)
           FROM Appointment a
           WHERE a.doctor.id = :doctorId
             AND a.status IN :statuses
             AND a.startAt < :newEnd
             AND a.endAt > :newStart
           """)
    boolean existsOverlappingAppointment(
            @Param("doctorId") Long doctorId,
            @Param("statuses") Collection<AppointmentStatus> statuses,
            @Param("newStart") LocalDateTime newStart,
            @Param("newEnd") LocalDateTime newEnd
    );

    @Query("""
    SELECT a
    FROM Appointment a
    WHERE a.doctor.id = :doctorId
      AND (:status IS NULL OR a.status = :status)
      AND (:from IS NULL OR a.startAt >= :from)
      AND (:to   IS NULL OR a.startAt <= :to)
      """)
    Page<Appointment> findDoctorAppointments(
            @Param("doctorId") Long doctorId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("status") AppointmentStatus status,
            Pageable pageable
    );

    @Query("""
        SELECT a
        FROM Appointment a
        WHERE a.patient.id = :patientId
          AND (:status IS NULL OR a.status = :status)
          AND (:from IS NULL OR a.startAt >= :from)
          AND (:to   IS NULL OR a.startAt <= :to)
        """)
    Page<Appointment> findPatientAppointments(
            @Param("patientId") Long patientId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("status") AppointmentStatus status,
            Pageable pageable
    );

    @Query("""
    SELECT COUNT(a) > 0
    FROM Appointment a
    WHERE a.doctor.id = :doctorId
      AND a.status IN :statuses
      AND a.id <> :excludeId
      AND a.startAt < :endAt
      AND a.endAt   > :startAt
""")
    boolean existsOverlappingAppointmentExcludingId(
            Long doctorId,
            EnumSet<AppointmentStatus> statuses,
            LocalDateTime startAt,
            LocalDateTime endAt,
            Long excludeId
    );
}
