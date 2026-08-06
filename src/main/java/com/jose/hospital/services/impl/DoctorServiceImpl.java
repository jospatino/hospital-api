package com.jose.hospital.services.impl;

import com.jose.hospital.domain.Doctor;
import com.jose.hospital.domain.Specialty;
import com.jose.hospital.dtos.DoctorRequest;
import com.jose.hospital.dtos.DoctorResponse;
import com.jose.hospital.dtos.DoctorUpdateRequest;
import com.jose.hospital.exceptions.BusinessValidationException;
import com.jose.hospital.exceptions.DoctorNotFoundException;
import com.jose.hospital.exceptions.EmailAlreadyExistsException;
import com.jose.hospital.mappers.DoctorMapper;
import com.jose.hospital.repositories.DoctorRepository;
import com.jose.hospital.services.DoctorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorMapper doctorMapper;

    public DoctorServiceImpl(DoctorRepository doctorRepository, DoctorMapper doctorMapper){
        this.doctorRepository = doctorRepository;
        this.doctorMapper = doctorMapper;
    }

    @Override
    @Transactional
    public DoctorResponse create(DoctorRequest doctorRequest) {

        if(doctorRepository.existsByEmail(doctorRequest.getEmail())){
            throw new EmailAlreadyExistsException("Doctor email already exists");
        }

        Doctor doctor = new Doctor();
        doctor.setActive(true);
        doctor.setName(doctorRequest.getName());
        doctor.setEmail(doctorRequest.getEmail());
        doctor.setSpecialty(doctorRequest.getSpecialty());
        doctor.setDateOfRegister(LocalDate.now());

        Doctor saved = doctorRepository.save(doctor);

        return doctorMapper.toResponse(saved);
    }

    @Override
    public DoctorResponse findById(Long id) {
        return doctorMapper.toResponse(this.doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException(id)));
    }

    @Override
    public List<DoctorResponse> findAll() {
        return doctorRepository.findAll()
                .stream()
                .map(doctorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorResponse> findAllActive() {
        return doctorRepository.findByActiveTrue()
                .stream()
                .map(doctorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorResponse> findBySpecialty(Specialty specialty) {
        return doctorRepository.findBySpecialty(specialty)
                .stream()
                .map(doctorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorResponse> findActiveBySpecialty(Specialty specialty) {
        return doctorRepository.findBySpecialtyAndActiveTrue(specialty)
                .stream()
                .map(doctorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DoctorResponse> findRegisteredAfter(LocalDate date) {
        return doctorRepository.findByDateOfRegisterAfter(date)
                .stream()
                .map(doctorMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DoctorResponse update(Long id, DoctorUpdateRequest doctorRequest) {
        Doctor doctor = doctorRepository.findById(id).orElseThrow(() -> new DoctorNotFoundException(id));

        if (doctorRequest.getName() != null)
            doctor.setName(doctorRequest.getName());
        if(doctorRequest.getSpecialty() != null)
            doctor.setSpecialty(doctorRequest.getSpecialty());
        if(doctorRequest.getEmail() != null && !doctorRequest.getEmail().equals(doctor.getEmail())){
            if(doctorRepository.existsByEmail(doctorRequest.getEmail())){
                throw new EmailAlreadyExistsException("Email already exists");
            }
            doctor.setEmail(doctorRequest.getEmail());
        }



        return doctorMapper.toResponse(doctor);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        Doctor doctor = doctorRepository.findById(id).orElseThrow(()-> new DoctorNotFoundException(id));

        if (!doctor.isActive()){
            throw new BusinessValidationException("Doctor is already deactivated");
        }
        doctor.setActive(false);
    }
}
