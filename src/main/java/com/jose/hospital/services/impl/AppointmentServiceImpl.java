package com.jose.hospital.services.impl;

import com.jose.hospital.domain.*;
import com.jose.hospital.dtos.AppointmentCreateRequest;
import com.jose.hospital.dtos.AppointmentRescheduleRequest;
import com.jose.hospital.dtos.AppointmentResponse;
import com.jose.hospital.exceptions.*;
import com.jose.hospital.mappers.AppointmentMapper;
import com.jose.hospital.repositories.AppointmentAuditEventRepository;
import com.jose.hospital.repositories.AppointmentRepository;
import com.jose.hospital.repositories.DoctorRepository;
import com.jose.hospital.repositories.PatientRepository;
import com.jose.hospital.services.AppointmentService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumSet;


@Service
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentAuditEventRepository auditEventRepository;

    public AppointmentServiceImpl(AppointmentRepository appointmentRepository, DoctorRepository doctorRepository,
                                  PatientRepository patientRepository, AppointmentAuditEventRepository auditEventRepository){
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.auditEventRepository = auditEventRepository;
    }

    @Override
    @Transactional
    public AppointmentResponse create(AppointmentCreateRequest request) {

        LocalDateTime now = LocalDateTime.now();

        // La cita debe iniciar en el futuro
        if (!request.getStartAt().isAfter(now)) {
            throw new BusinessValidationException(
                    "Appointment start time must be in the future"
            );
        }

        // La hora de inicio debe ser antes de la hora de fin
        if (!request.getStartAt().isBefore(request.getEndAt())) {
            throw new InvalidAppointmentTimeException(
                    "End time must be after start time"
            );
        }

        Doctor doctor = doctorRepository.findByIdAndActiveTrue(request.getDoctorId())
                .orElseThrow(() -> new DoctorNotFoundException(request.getDoctorId()));

        Patient patient = patientRepository.findByIdAndActiveTrue(request.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException(request.getPatientId()));

        // El paciente debe pertenecer al doctor
        if (!patient.getDoctor().getId().equals(doctor.getId())) {
            throw new BusinessValidationException(
                    "Patient is not assigned to this doctor"
            );
        }

        boolean overlap = appointmentRepository.existsOverlappingAppointment(
                doctor.getId(),
                EnumSet.of(AppointmentStatus.SCHEDULED),
                request.getStartAt(),
                request.getEndAt()
        );

        if (overlap) {
            throw new AppointmentOverlapException(
                    "Appointment overlaps with an existing appointment"
            );
        }

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setStartAt(request.getStartAt());
        appointment.setEndAt(request.getEndAt());
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setCreatedAt(now);

        Appointment appointmentSaved = appointmentRepository.save(appointment);

        AppointmentAuditEvent auditEvent = new AppointmentAuditEvent();
        auditEvent.setAppointment(appointmentSaved);
        auditEvent.setType(AuditEventType.CREATED);
        auditEvent.setCreatedAt(now);
        auditEvent.setPerformedBy("system");
        auditEvent.setDetails("Appointment created");

        auditEventRepository.save(auditEvent);

        return AppointmentMapper.toResponse(appointmentSaved);
    }

    @Override
    @Transactional
    public AppointmentResponse cancel(Long id) {

        LocalDateTime now = LocalDateTime.now();

        Appointment appointment = this.appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED){
            throw new InvalidAppointmentStatusException("Appointment has to have Scheduled status");
        }

        if (!now.isBefore(appointment.getStartAt())){
            throw new AppointmentCannotBeCancelledException("Appointment has already started");
        }

        appointment.cancel(now);
        Appointment savedAppointment = this.appointmentRepository.save(appointment);

        // Audit
        AppointmentAuditEvent auditEvent = new AppointmentAuditEvent();
        auditEvent.setAppointment(savedAppointment);
        auditEvent.setType(AuditEventType.CANCELLED);
        auditEvent.setCreatedAt(now);
        auditEvent.setPerformedBy("system");
        auditEvent.setDetails("Appointment cancelled");
        auditEventRepository.save(auditEvent);

        return AppointmentMapper.toResponse(savedAppointment);
    }

    @Override
    public Page<AppointmentResponse> getDoctorAppointments(Long doctorId, LocalDateTime from, LocalDateTime to, AppointmentStatus status, Pageable pageable) {

        doctorRepository.findByIdAndActiveTrue(doctorId).orElseThrow(() -> new DoctorNotFoundException(doctorId));

        if(from != null && to != null && from.isAfter(to)){
            throw new BusinessValidationException("'from' must be before or equal to 'to'");
        }

        AppointmentStatus effectiveStatus = (status!=null) ? status : AppointmentStatus.SCHEDULED;

        Page<Appointment> page = appointmentRepository.findDoctorAppointments(doctorId, from, to, effectiveStatus, pageable);

        return page.map(AppointmentMapper::toResponse);

    }

    @Override
    public Page<AppointmentResponse> getPatientAppointments(Long patientId, LocalDateTime from, LocalDateTime to, AppointmentStatus status, Pageable pageable) {

        patientRepository.findByIdAndActiveTrue(patientId).orElseThrow(() -> new PatientNotFoundException(patientId));

        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessValidationException("'from' must be before or equal to 'to'");
        }
        AppointmentStatus effectiveStatus = status != null ? status : AppointmentStatus.SCHEDULED;

        Page<Appointment> page = appointmentRepository.findPatientAppointments(
                patientId,
                from,
                to,
                effectiveStatus,
                pageable
        );
        return page.map(AppointmentMapper::toResponse);
    }

    @Override
    @Transactional
    public AppointmentResponse completeAppointment(Long id) {

        LocalDateTime now = LocalDateTime.now();

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new InvalidAppointmentStatusException(
                    "Only scheduled appointments can be completed"
            );
        }

        if (appointment.getEndAt().isAfter(now)) {
            throw new BusinessValidationException(
                    "Appointment cannot be completed before it ends"
            );
        }

        appointment.setStatus(AppointmentStatus.COMPLETED);

        Appointment saved = appointmentRepository.save(appointment);

        AppointmentAuditEvent auditEvent = new AppointmentAuditEvent(
                saved,
                AuditEventType.COMPLETED,
                now,
                "system",
                "Appointment completed"
        );

        auditEventRepository.save(auditEvent);

        return AppointmentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AppointmentResponse reschedule(Long id, AppointmentRescheduleRequest request) {

        LocalDateTime now = LocalDateTime.now();

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));

        if (appointment.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new InvalidAppointmentStatusException(
                    "Appointment must have SCHEDULED status"
            );
        }

        if (!now.isBefore(appointment.getStartAt())) {
            throw new BusinessValidationException(
                    "Cannot reschedule an appointment that has already started"
            );
        }

        if (!request.getStartAt().isAfter(now)) {
            throw new BusinessValidationException(
                    "The new appointment time must be in the future"
            );
        }

        if (!request.getStartAt().isBefore(request.getEndAt())) {
            throw new InvalidAppointmentTimeException(
                    "End time must be after start time"
            );
        }

        boolean overlap = appointmentRepository.existsOverlappingAppointmentExcludingId(
                appointment.getDoctor().getId(),
                EnumSet.of(AppointmentStatus.SCHEDULED),
                request.getStartAt(),
                request.getEndAt(),
                appointment.getId()
        );

        if (overlap) {
            throw new AppointmentOverlapException(
                    "Appointment overlaps with an existing appointment"
            );
        }

        LocalDateTime oldStart = appointment.getStartAt();
        LocalDateTime oldEnd = appointment.getEndAt();

        appointment.setStartAt(request.getStartAt());
        appointment.setEndAt(request.getEndAt());

        Appointment saved = appointmentRepository.save(appointment);

        AppointmentAuditEvent auditEvent = new AppointmentAuditEvent();
        auditEvent.setAppointment(saved);
        auditEvent.setType(AuditEventType.RESCHEDULED);
        auditEvent.setCreatedAt(now);
        auditEvent.setPerformedBy("system");
        auditEvent.setDetails(
                "Appointment rescheduled from "
                        + oldStart + " - " + oldEnd
                        + " to "
                        + saved.getStartAt() + " - " + saved.getEndAt()
        );

        auditEventRepository.save(auditEvent);

        return AppointmentMapper.toResponse(saved);
    }
}
