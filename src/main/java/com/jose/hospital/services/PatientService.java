package com.jose.hospital.services;

import com.jose.hospital.dtos.PatientCreateRequest;
import com.jose.hospital.dtos.PatientResponse;
import com.jose.hospital.dtos.PatientUpdateRequest;

import java.util.List;

public interface PatientService {
    PatientResponse getById(Long id);
    List<PatientResponse> findAll();
    PatientResponse create(PatientCreateRequest request);
    PatientResponse update(Long id, PatientUpdateRequest request);
    void softDelete(Long id);
}
