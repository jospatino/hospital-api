package com.jose.hospital.services;

import com.jose.hospital.domain.Specialty;
import com.jose.hospital.dtos.DoctorRequest;
import com.jose.hospital.dtos.DoctorResponse;
import com.jose.hospital.dtos.DoctorUpdateRequest;

import java.time.LocalDate;
import java.util.List;

public interface DoctorService {
    public DoctorResponse create(DoctorRequest doctorRequest);
    public DoctorResponse findById(Long id);
    public List<DoctorResponse> findAll();
    public List<DoctorResponse> findAllActive();
    public List<DoctorResponse> findBySpecialty(Specialty specialty);
    public List<DoctorResponse> findActiveBySpecialty (Specialty specialty);
    public List<DoctorResponse> findRegisteredAfter(LocalDate date);
    public DoctorResponse update(Long id, DoctorUpdateRequest doctorRequest);
    public void deactivate(Long id);
}
