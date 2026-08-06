package com.jose.hospital.repositories;

import com.jose.hospital.domain.Doctor;
import com.jose.hospital.domain.Patient;
import com.jose.hospital.domain.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findBySpecialty(Specialty specialty);
    boolean existsByEmail(String email);
    Optional<Doctor> findByEmail(String email);
    Optional<Doctor> findByIdAndActiveTrue(Long id);
    List<Doctor> findByActiveTrue();
    List<Doctor> findBySpecialtyAndActiveTrue(Specialty specialty);
    List<Doctor> findByDateOfRegisterAfter(LocalDate localDate);
}

