package com.jose.hospital.services.impl;

import com.jose.hospital.domain.Patient;
import com.jose.hospital.dtos.PatientCreateRequest;
import com.jose.hospital.dtos.PatientResponse;
import com.jose.hospital.dtos.PatientUpdateRequest;
import com.jose.hospital.exceptions.*;
import com.jose.hospital.mappers.PatientMapper;
import com.jose.hospital.repositories.DoctorRepository;
import com.jose.hospital.repositories.PatientRepository;
import com.jose.hospital.services.PatientService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;
    private final DoctorRepository doctorRepository;

    public PatientServiceImpl(PatientRepository patientRepository, PatientMapper mapper, DoctorRepository doctorRepository){
        this.patientMapper = mapper;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
    }
    @Override
    public PatientResponse getById(Long id) {

        Patient patient = patientRepository.findById(id).orElseThrow(() -> new PatientNotFoundException(id));
        return patientMapper.toResponse(patient);
    }

    @Override
    public List<PatientResponse> findAll() {
        return patientRepository.findByActiveTrue()
                .stream()
                .map(patientMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public PatientResponse create(PatientCreateRequest request) {

        if(patientRepository.existsByEmail(request.getEmail()))
            throw new EmailAlreadyExistsException(request.getEmail());
        if(request.getDoctorId() == null)
            throw new NullDoctorException();

        Patient patient = new Patient();
        patient.setActive(true);
        patient.setName(request.getName());
        patient.setEmail(request.getEmail());
        patient.setDoctor(doctorRepository.findByIdAndActiveTrue(request.getDoctorId()).orElseThrow(() -> new DoctorNotFoundException(request.getDoctorId())));
        patient.setDateOfRegistry(LocalDate.now());
        Patient saved = patientRepository.save(patient);
        return patientMapper.toResponse(saved);
    }
    @Transactional
    @Override
    public PatientResponse update(Long id, PatientUpdateRequest request) {

        Patient patient = patientRepository.findById(id).orElseThrow(() -> new PatientNotFoundException(id));

        if(request.getEmail() != null){
            if(!request.getEmail().equals(patient.getEmail())) {
                if (patientRepository.existsByEmail(request.getEmail()))
                    throw new EmailAlreadyExistsException(request.getEmail());
            }
        }

        if(request.getDoctorId() != null){
            patient.setDoctor(doctorRepository.findByIdAndActiveTrue(request.getDoctorId()).orElseThrow(() -> new DoctorNotFoundException(request.getDoctorId())));
        }

        if (request.getName() != null){
            patient.setName(request.getName());
        }

        if (request.getEmail() != null){
            patient.setEmail(request.getEmail());
        }
        return patientMapper.toResponse(patient);
    }

    @Override
    @Transactional
    public void softDelete(Long id) {
        Patient patient = patientRepository.findById(id).orElseThrow(() -> new PatientNotFoundException(id));
        if(!patient.isActive())
            throw new BusinessValidationException("Patient already deactivated");
        patient.setActive(false);
    }
}
