package com.jose.hospital.controllers;

import com.jose.hospital.domain.AppointmentStatus;
import com.jose.hospital.dtos.AppointmentResponse;
import com.jose.hospital.dtos.PatientCreateRequest;
import com.jose.hospital.dtos.PatientResponse;
import com.jose.hospital.dtos.PatientUpdateRequest;
import com.jose.hospital.services.AppointmentService;
import com.jose.hospital.services.PatientService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    private final PatientService patientService;
    private final AppointmentService appointmentService;

    public PatientController(PatientService patientService, AppointmentService appointmentService) {

        this.patientService = patientService;
        this.appointmentService = appointmentService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientResponse> getById(@PathVariable Long id){
        return ResponseEntity.ok(patientService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<PatientResponse>> getAll(){
        return ResponseEntity.ok(patientService.findAll());
    }

    @PostMapping
    public ResponseEntity<PatientResponse> createPatient(@Valid @RequestBody PatientCreateRequest patientCreateRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(patientService.create(patientCreateRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponse> updatePatient(@PathVariable Long id, @Valid @RequestBody PatientUpdateRequest patientUpdateRequest){
        return ResponseEntity.ok(patientService.update(id, patientUpdateRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id){
        patientService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{patientId}/appointments")
    public ResponseEntity<Page<AppointmentResponse>> getPatientAppointments(
            @PathVariable Long patientId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to,

            @RequestParam(required = false)
            AppointmentStatus status,

            @PageableDefault(size = 20, sort = "startAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        return ResponseEntity.ok(
                appointmentService.getPatientAppointments(patientId, from, to, status, pageable)
        );
    }
}