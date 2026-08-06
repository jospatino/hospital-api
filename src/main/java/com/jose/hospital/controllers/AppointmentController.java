package com.jose.hospital.controllers;

import com.jose.hospital.domain.AppointmentStatus;
import com.jose.hospital.dtos.AppointmentCreateRequest;
import com.jose.hospital.dtos.AppointmentRescheduleRequest;
import com.jose.hospital.dtos.AppointmentResponse;
import com.jose.hospital.services.AppointmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService){
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponse> createAppointment(@Valid @RequestBody AppointmentCreateRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.create(request));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponse> cancelAppointment(@PathVariable Long id){
        return ResponseEntity.ok(appointmentService.cancel(id));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<Page<AppointmentResponse>> getByDoctor(@PathVariable Long doctorId,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                                                  @RequestParam (required = false) AppointmentStatus status,
                                                  @PageableDefault(size = 20, sort = "startAt") Pageable pageable){
        return ResponseEntity.ok(
                this.appointmentService.getDoctorAppointments(
                        doctorId,
                        from,
                        to,
                        status,
                        pageable));
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<Page<AppointmentResponse>> getByPatient(@PathVariable Long patientId,
                                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                                                                   @RequestParam(required = false) AppointmentStatus status,
                                                                   @PageableDefault(size = 20, sort = "startAt") Pageable pageable){
        return ResponseEntity.ok(
                this.appointmentService.getPatientAppointments(
                        patientId,
                        from,
                        to,
                        status,
                        pageable));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<AppointmentResponse> completeAppointment(@PathVariable Long id){
        return ResponseEntity.ok(this.appointmentService.completeAppointment(id));
    }

    @PatchMapping("/{id}/reschedule")
    public ResponseEntity<AppointmentResponse> rescheduleAppointment(@PathVariable Long id, @Valid @RequestBody AppointmentRescheduleRequest request){
        return ResponseEntity.ok(this.appointmentService.reschedule(id, request));
    }
}
