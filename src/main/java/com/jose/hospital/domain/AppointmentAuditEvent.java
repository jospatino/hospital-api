package com.jose.hospital.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointment_audit_event")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AppointmentAuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "appointment_id", nullable = false)
    private Appointment appointment;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditEventType type;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    @Column(name = "performed_by", nullable = false)
    private String performedBy;
    @Column
    private String details;

    public AppointmentAuditEvent(Appointment appointment, AuditEventType type, LocalDateTime createdAt, String performedBy, String details) {
        this.appointment = appointment;
        this.type = type;
        this.createdAt = createdAt;
        this.performedBy = performedBy;
        this.details = details;
    }
}
