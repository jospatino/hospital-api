package com.jose.hospital.controllers;

import com.jose.hospital.domain.Specialty;
import com.jose.hospital.dtos.DoctorRequest;
import com.jose.hospital.dtos.DoctorResponse;
import com.jose.hospital.dtos.DoctorUpdateRequest;
import com.jose.hospital.services.DoctorService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @PostMapping
    public ResponseEntity<DoctorResponse> createDoctor(@Valid @RequestBody DoctorRequest doctorRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(doctorService.create(doctorRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> getById(@PathVariable Long id){
        return ResponseEntity.ok(doctorService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<DoctorResponse>> getAll(){
        return ResponseEntity.ok(doctorService.findAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<DoctorResponse>> getAllActive(){
        return ResponseEntity.ok(doctorService.findAllActive());
    }

    @GetMapping("/specialty")
    public ResponseEntity<List<DoctorResponse>> getBySpecialty(@RequestParam Specialty specialty){
        return ResponseEntity.ok(doctorService.findBySpecialty(specialty));
    }

    @GetMapping("/active-specialty")
    public ResponseEntity<List<DoctorResponse>> getActiveBySpecialty(@RequestParam Specialty specialty){
        return ResponseEntity.ok(doctorService.findActiveBySpecialty(specialty));
    }

    @GetMapping("/registered-after")
    public ResponseEntity<List<DoctorResponse>> getRegisteredAfter(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<DoctorResponse> doctors = doctorService.findRegisteredAfter(date);
        return ResponseEntity.ok(doctors);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DoctorResponse> updateDoctor(@PathVariable Long id, @Valid @RequestBody DoctorUpdateRequest request){
        return ResponseEntity.ok(doctorService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id){
        doctorService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
