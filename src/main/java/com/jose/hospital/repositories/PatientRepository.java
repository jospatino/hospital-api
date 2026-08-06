package com.jose.hospital.repositories;

import com.jose.hospital.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    List<Patient>  findByDoctorId(Long id);
    Optional<Patient> findByIdAndActiveTrue(Long id);
    boolean existsByEmail(String email);
    List<Patient> findByActiveTrue();
    List<Patient> findByNameContainingIgnoreCase(String nombre);
    List<Patient> findByDoctorNameContainingIgnoreCase(String nombre);
    List<Patient> findByDateOfRegistryAfter(LocalDate fechaRegistro);


}


