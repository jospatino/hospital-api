package com.jose.hospital.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "doctor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Doctor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 150)
    private String name;
    @Column(nullable = false, unique = true, length = 150)
    private String email;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 150)
    private Specialty specialty;
    @Column(name = "date_of_registry", nullable = false)
    private LocalDate dateOfRegister;
    @Column(nullable = false)
    private boolean active;
    @OneToMany(mappedBy = "doctor", fetch = FetchType.LAZY)
    private List<Patient> patients = new ArrayList<>();
}


