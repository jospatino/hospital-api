package com.jose.hospital.services.impl;

import com.jose.hospital.domain.*;
import com.jose.hospital.dtos.AppointmentCreateRequest;
import com.jose.hospital.dtos.AppointmentRescheduleRequest;
import com.jose.hospital.dtos.AppointmentResponse;
import com.jose.hospital.exceptions.*;
import com.jose.hospital.repositories.AppointmentAuditEventRepository;
import com.jose.hospital.repositories.AppointmentRepository;
import com.jose.hospital.repositories.DoctorRepository;
import com.jose.hospital.repositories.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceImplTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AppointmentAuditEventRepository auditEventRepository;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private Doctor doctor;
    private Patient patient;

    @BeforeEach
    void setUp() {
        doctor = new Doctor();
        doctor.setId(10L);
        doctor.setActive(true);

        patient = new Patient();
        patient.setId(20L);
        patient.setActive(true);
        patient.setDoctor(doctor);
    }



    private AppointmentCreateRequest buildValidRequest() {
        AppointmentCreateRequest request = new AppointmentCreateRequest();
        request.setDoctorId(doctor.getId());
        request.setPatientId(patient.getId());

        LocalDateTime startAt = LocalDateTime.now().plusDays(1);
        request.setStartAt(startAt);
        request.setEndAt(startAt.plusMinutes(45));

        return request;
    }


    @Test
    void create_shouldSaveAppointmentAndAudit_andReturnResponse_whenValidRequest() {
        // given
        AppointmentCreateRequest request = buildValidRequest();

        when(doctorRepository.findByIdAndActiveTrue(request.getDoctorId()))
                .thenReturn(Optional.of(doctor));

        when(patientRepository.findByIdAndActiveTrue(request.getPatientId()))
                .thenReturn(Optional.of(patient));

        when(appointmentRepository.existsOverlappingAppointment(
                eq(doctor.getId()),
                any(EnumSet.class),
                eq(request.getStartAt()),
                eq(request.getEndAt())
        )).thenReturn(false);

        when(appointmentRepository.save(any(Appointment.class)))
                .thenAnswer(inv -> {
                    Appointment a = inv.getArgument(0);
                    a.setId(100L);
                    return a;
                });

        when(auditEventRepository.save(any(AppointmentAuditEvent.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<Appointment> appointmentCaptor = ArgumentCaptor.forClass(Appointment.class);
        ArgumentCaptor<AppointmentAuditEvent> auditCaptor = ArgumentCaptor.forClass(AppointmentAuditEvent.class);

        // when
        AppointmentResponse response = appointmentService.create(request);

        // then (response)
        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(doctor.getId(), response.getDoctorId());
        assertEquals(patient.getId(), response.getPatientId());
        assertEquals(AppointmentStatus.SCHEDULED, response.getStatus());
        assertEquals(request.getStartAt(), response.getStartAt());
        assertEquals(request.getEndAt(), response.getEndAt());
        assertNotNull(response.getCreatedAt());

        // then (interactions + captors)
        verify(appointmentRepository, times(1)).save(appointmentCaptor.capture());
        verify(auditEventRepository, times(1)).save(auditCaptor.capture());

        Appointment savedAppointment = appointmentCaptor.getValue();
        assertEquals(doctor, savedAppointment.getDoctor());
        assertEquals(patient, savedAppointment.getPatient());
        assertEquals(request.getStartAt(), savedAppointment.getStartAt());
        assertEquals(request.getEndAt(), savedAppointment.getEndAt());
        assertEquals(AppointmentStatus.SCHEDULED, savedAppointment.getStatus());
        assertNotNull(savedAppointment.getCreatedAt());

        AppointmentAuditEvent savedAudit = auditCaptor.getValue();
        assertNotNull(savedAudit.getAppointment());
        assertEquals(100L, savedAudit.getAppointment().getId());
        assertEquals(AuditEventType.CREATED, savedAudit.getType());
        assertNotNull(savedAudit.getCreatedAt());
        assertEquals("system", savedAudit.getPerformedBy());
        assertEquals("Appointment created", savedAudit.getDetails());

        // y que sí pasó por la validación de overlap
        verify(appointmentRepository, times(1)).existsOverlappingAppointment(
                eq(doctor.getId()),
                any(EnumSet.class),
                eq(request.getStartAt()),
                eq(request.getEndAt())
        );
    }


    @Test
    void create_shouldThrowDoctorNotFound_andNotSaveAnything_whenDoctorNotFound() {
        // Arrange
        AppointmentCreateRequest request = buildValidRequest();

        when(doctorRepository.findByIdAndActiveTrue(request.getDoctorId()))
                .thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(DoctorNotFoundException.class, () -> appointmentService.create(request));

        // Verify
        verify(appointmentRepository, never()).save(any());
        verify(auditEventRepository, never()).save(any());
        verify(patientRepository, never()).findByIdAndActiveTrue(any());
        verify(appointmentRepository, never()).existsOverlappingAppointment(anyLong(), any(), any(), any());
    }

    @Test
    void create_shouldThrowPatientNotFound_andNotSaveAnything_whenPatientNotFound() {
        // given
        AppointmentCreateRequest request = buildValidRequest();

        when(doctorRepository.findByIdAndActiveTrue(request.getDoctorId())).thenReturn(Optional.of(doctor));
        when(patientRepository.findByIdAndActiveTrue(request.getPatientId())).thenReturn(Optional.empty());

        assertThrows(PatientNotFoundException.class, () ->
                appointmentService.create(request));

        verify(appointmentRepository, never()).save(any());
        verify(auditEventRepository, never()).save(any());
        verify(appointmentRepository, never()).existsOverlappingAppointment(anyLong(), any(), any(), any());

    }


    @Test
    void create_shouldThrowBusinessValidationException_whenPatientBelongsToAnotherDoctor() {

        AppointmentCreateRequest request = buildValidRequest();

        Doctor anotherDoctor = new Doctor();
        anotherDoctor.setId(99L);
        anotherDoctor.setActive(true);

        patient.setDoctor(anotherDoctor);

        when(doctorRepository.findByIdAndActiveTrue(request.getDoctorId()))
                .thenReturn(Optional.of(doctor));

        when(patientRepository.findByIdAndActiveTrue(request.getPatientId()))
                .thenReturn(Optional.of(patient));

        assertThrows(
                BusinessValidationException.class,
                () -> appointmentService.create(request)
        );

        verify(doctorRepository)
                .findByIdAndActiveTrue(request.getDoctorId());

        verify(patientRepository)
                .findByIdAndActiveTrue(request.getPatientId());

        verify(appointmentRepository, never())
                .existsOverlappingAppointment(anyLong(), any(), any(), any());

        verify(appointmentRepository, never()).save(any());
        verify(auditEventRepository, never()).save(any());
    }

    @Test
    void create_shouldThrowBusinessValidationException_whenStartTimeIsInThePast(){

        LocalDateTime startAt = LocalDateTime.now().minusHours(2);
        AppointmentCreateRequest request = buildValidRequest();
        request.setStartAt(startAt);
        request.setEndAt(startAt.plusMinutes(45));

        assertThrows(BusinessValidationException.class, () -> appointmentService.create(request));

        verifyNoInteractions(
                doctorRepository,
                patientRepository,
                appointmentRepository,
                auditEventRepository
        );
    }


    @Test
    void create_shouldThrowInvalidAppointmentTime_andNotTouchRepos_whenStartIsNotBeforeEnd() {

        AppointmentCreateRequest request = buildValidRequest();

        request.setEndAt(request.getStartAt());

        assertThrows(InvalidAppointmentTimeException.class, () -> appointmentService.create(request));

        verifyNoInteractions(doctorRepository, patientRepository, appointmentRepository, auditEventRepository);
    }


    @Test
    void create_shouldThrowOverlap_andNotSaveAnything_whenOverlappingAppointmentExist() {
        // given
      AppointmentCreateRequest request = buildValidRequest();

      when(doctorRepository.findByIdAndActiveTrue(request.getDoctorId())).thenReturn(Optional.of(doctor));
      when(patientRepository.findByIdAndActiveTrue(request.getPatientId())).thenReturn(Optional.of(patient));
      when(appointmentRepository.existsOverlappingAppointment(request.getDoctorId(), EnumSet.of(AppointmentStatus.SCHEDULED),
              request.getStartAt(), request.getEndAt())).thenReturn(true);

      assertThrows(AppointmentOverlapException.class, () ->
              appointmentService.create(request));

      verify(doctorRepository).findByIdAndActiveTrue(request.getDoctorId());
      verify(patientRepository).findByIdAndActiveTrue(request.getPatientId());
      verify(auditEventRepository, never()).save(any());
      verify(appointmentRepository, never()).save(any());
      verify(appointmentRepository).existsOverlappingAppointment(
                eq(request.getDoctorId()),
                eq(EnumSet.of(AppointmentStatus.SCHEDULED)),
                eq(request.getStartAt()),
                eq(request.getEndAt())
      );
    }

    @Test
    void cancel_shouldSaveAppointment_andUpdateStatus(){
        LocalDateTime now = LocalDateTime.now();

        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setStartAt(now.plusMinutes(10));
        appointment.setEndAt(now.plusMinutes(40));

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));



        AppointmentResponse response = appointmentService.cancel(1L);


        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(AppointmentStatus.CANCELLED, appointment.getStatus());
        assertNotNull(response.getCancelledAt());
        verify(appointmentRepository).save(appointment);
        verify(auditEventRepository).save(any(AppointmentAuditEvent.class));
        verify(appointmentRepository).findById(1L);

    }

    @Test
    void cancel_shouldThrowInvalidAppointmentStatus_whenAppointmentIsNotScheduled() {

        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointment.setStartAt(LocalDateTime.now().plusHours(1));

        when(appointmentRepository.findById(1L))
                .thenReturn(Optional.of(appointment));

        assertThrows(
                InvalidAppointmentStatusException.class,
                () -> appointmentService.cancel(1L)
        );

        verify(appointmentRepository).findById(1L);
        verify(appointmentRepository, never()).save(any());
        verify(auditEventRepository, never()).save(any());
    }

    @Test
    void cancel_shouldThrowAppointmentNotFound_whenIdDoesNotExist(){

        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AppointmentNotFoundException.class, () -> appointmentService.cancel(1L));
        verify(appointmentRepository).findById(1L);
        verify(appointmentRepository, never()).save(any(Appointment.class));
        verify(auditEventRepository, never()).save(any());
    }

    @Test
    void cancel_shouldThrowAppointmentCannotBeCancelled_whenAppointmentAlreadyStarted(){
        LocalDateTime now = LocalDateTime.now();
        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setStartAt(now.minusMinutes(40));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThrows(AppointmentCannotBeCancelledException.class, () -> appointmentService.cancel(1L));

        verify(appointmentRepository, never()).save(any(Appointment.class));
        verify(auditEventRepository, never()).save(any());
    }

    @Test
    void complete_shouldCompleteAppointment_andSaveAudit_whenAppointmentHasEnded(){
        LocalDateTime now = LocalDateTime.now();
        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setStartAt(now.minusHours(1));
        appointment.setEndAt(now.minusMinutes(40));
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> inv.getArgument(0));

        AppointmentResponse response = appointmentService.completeAppointment(1L);

        assertEquals(AppointmentStatus.COMPLETED, response.getStatus());
        assertEquals(AppointmentStatus.COMPLETED, appointment.getStatus());
        assertNotNull(response);

        verify(appointmentRepository).findById(1L);
        verify(auditEventRepository).save(any(AppointmentAuditEvent.class));
        verify(appointmentRepository).save(appointment);

    }

    @Test
    void complete_shouldThrowAppointmentNotFound_whenAppointmentDoesNotExist(){

        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AppointmentNotFoundException.class, () -> appointmentService.completeAppointment(1L));
        verify(appointmentRepository).findById(1L);
        verify(appointmentRepository, never()).save(any());
        verify(auditEventRepository, never()).save(any());
    }

    @Test
    void complete_shouldThrowInvalidAppointmentStatusException_whenAppointmentIsNotScheduled(){

        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.COMPLETED);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

         assertThrows(InvalidAppointmentStatusException.class, () -> appointmentService.completeAppointment(1L));

         verify(appointmentRepository).findById(1L);
         verify(appointmentRepository, never()).save(any(Appointment.class));
         verify(auditEventRepository, never()).save(any(AppointmentAuditEvent.class));

    }

    @Test
    void complete_shouldThrowBusinessValidationException_whenAppointmentHasNotEnded(){

        Appointment appointment = new Appointment();
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setEndAt(LocalDateTime.now().plusMinutes(5));

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThrows(BusinessValidationException.class, () -> appointmentService.completeAppointment(1L));

        verify(appointmentRepository).findById(1L);
        verify(appointmentRepository, never()).save(any(Appointment.class));
        verify(auditEventRepository, never()).save(any(AppointmentAuditEvent.class));
    }

    @Test
    void reschedule_shouldUpdateTimesSaveAppointmentAndAudit_whenRequestIsValid(){
        LocalDateTime now = LocalDateTime.now();
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setStartAt(now.plusHours(2));
        appointment.setEndAt(now.plusHours(3));
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        AppointmentRescheduleRequest request = new AppointmentRescheduleRequest();
        request.setStartAt(now.plusHours(3));
        request.setEndAt(now.plusHours(4));

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        when(appointmentRepository.existsOverlappingAppointmentExcludingId(
                appointment.getDoctor().getId(),
                EnumSet.of(AppointmentStatus.SCHEDULED),
                request.getStartAt(),
                request.getEndAt(),
                appointment.getId()
        )).thenReturn(false);

        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> (inv.getArgument(0)));
        when(auditEventRepository.save(any(AppointmentAuditEvent.class))).thenAnswer(inv -> (inv.getArgument(0)));


        ArgumentCaptor<Appointment> appointmentCaptor = ArgumentCaptor.forClass(Appointment.class);
        ArgumentCaptor<AppointmentAuditEvent> auditCaptor = ArgumentCaptor.forClass(AppointmentAuditEvent.class);

        AppointmentResponse response = appointmentService.reschedule(1L, request);

        assertEquals(1L, response.getId());
        assertEquals(request.getStartAt(), response.getStartAt());
        assertEquals(request.getEndAt(), response.getEndAt());
        assertEquals(AppointmentStatus.SCHEDULED, response.getStatus());

        verify(appointmentRepository).save(appointmentCaptor.capture());
        Appointment appointmentSaved = appointmentCaptor.getValue();

        assertEquals(1L, appointmentSaved.getId());
        assertEquals(request.getStartAt(), appointmentSaved.getStartAt());
        assertEquals(request.getEndAt(), appointmentSaved.getEndAt());
        assertEquals(AppointmentStatus.SCHEDULED, appointmentSaved.getStatus());

        verify(auditEventRepository).save(auditCaptor.capture());
        AppointmentAuditEvent auditSaved = auditCaptor.getValue();

        assertEquals(AuditEventType.RESCHEDULED, auditSaved.getType());
        assertEquals(appointmentSaved, auditSaved.getAppointment());
        assertNotNull(auditSaved.getCreatedAt());


    }

    @Test
    void reschedule_shouldThrowAppointmentNotFound_whenAppointmentDoesNotExist(){
        LocalDateTime now = LocalDateTime.now();

        AppointmentRescheduleRequest request = new AppointmentRescheduleRequest();
        request.setStartAt(now.plusHours(3));
        request.setEndAt(now.plusHours(4));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AppointmentNotFoundException.class, ()-> appointmentService.reschedule(1L, request));

        verify(appointmentRepository, never()).save(any(Appointment.class));
        verify(auditEventRepository, never()).save(any(AppointmentAuditEvent.class));
        verify(appointmentRepository, never()).existsOverlappingAppointmentExcludingId(any(), any(), any(), any(), any());
        verify(appointmentRepository).findById(1L);
    }

    @Test
    void reschedule_shouldThrowInvalidAppointmentStatus_whenAppointmentIsNotScheduled(){
        LocalDateTime now = LocalDateTime.now();

        AppointmentRescheduleRequest request = new AppointmentRescheduleRequest();
        request.setStartAt(now.plusHours(3));
        request.setEndAt(now.plusHours(4));
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.COMPLETED);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThrows(InvalidAppointmentStatusException.class, ()-> appointmentService.reschedule(1L, request));

        verify(appointmentRepository).findById(1L);
        verify(appointmentRepository, never()).save(any(Appointment.class));
        verify(auditEventRepository, never()).save(any(AppointmentAuditEvent.class));
        verify(appointmentRepository, never()).existsOverlappingAppointmentExcludingId(any(), any(), any(), any(), any());
    }

    @Test
    void reschedule_shouldThrowBusinessValidationException_whenNewStartTimeIsInThePast(){
        AppointmentRescheduleRequest request = new AppointmentRescheduleRequest();
        LocalDateTime newStartAt = LocalDateTime.now().minusHours(8);

        request.setStartAt(newStartAt);
        request.setEndAt(newStartAt.plusHours(1));
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setStartAt(LocalDateTime.now().plusDays(1));
        appointment.setEndAt(LocalDateTime.now().plusDays(1).plusHours(1));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThrows(BusinessValidationException.class, ()-> appointmentService.reschedule(1L, request));

        verify(appointmentRepository).findById(1L);

        verify(appointmentRepository, never())
                .existsOverlappingAppointmentExcludingId(
                        anyLong(), any(), any(), any(), anyLong()
                );

        verify(appointmentRepository, never()).save(any());
        verify(auditEventRepository, never()).save(any());
    }

    @Test
    void reschedule_shouldThrowInvalidAppointmentTime_whenStartIsNotBeforeEnd(){
        AppointmentRescheduleRequest request = new AppointmentRescheduleRequest();
        request.setStartAt(LocalDateTime.now().plusDays(2));
        request.setEndAt(LocalDateTime.now().plusDays(1));
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setStartAt(LocalDateTime.now().plusDays(1));
        appointment.setEndAt(LocalDateTime.now().plusDays(1).plusHours(1));
        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));

        assertThrows(InvalidAppointmentTimeException.class, () -> appointmentService.reschedule(1L, request));

        verify(appointmentRepository, never()).save(any(Appointment.class));
        verify(auditEventRepository, never()).save(any(AppointmentAuditEvent.class));
        verify(appointmentRepository, never()).existsOverlappingAppointmentExcludingId(any(), any(), any(), any(), any());

    }

    @Test
    void reschedule_shouldThrowAppointmentOverlap_whenNewTimeOverlapsAnotherAppointment(){
        Appointment appointment = new Appointment();
        appointment.setId(1L);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setStartAt(LocalDateTime.now().plusDays(2));
        appointment.setEndAt(appointment.getStartAt().plusHours(1));
        AppointmentRescheduleRequest request = new AppointmentRescheduleRequest();
        request.setStartAt(appointment.getStartAt().plusDays(1));
        request.setEndAt(request.getStartAt().plusHours(1));

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentRepository.existsOverlappingAppointmentExcludingId(
                eq(doctor.getId()),
                eq(EnumSet.of(AppointmentStatus.SCHEDULED)),
                eq(request.getStartAt()),
                eq(request.getEndAt()),
                eq(appointment.getId())
        )).thenReturn(true);

        assertThrows(AppointmentOverlapException.class, () -> appointmentService.reschedule(1L, request));

        verify(appointmentRepository).findById(1L);
        verify(appointmentRepository, never()).save(any(Appointment.class));
        verify(auditEventRepository, never()).save(any(AppointmentAuditEvent.class));
    }

}
