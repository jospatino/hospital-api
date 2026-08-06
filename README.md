Hospital Appointment System

A backend system designed to manage appointments between doctors and patients, focusing on clean domain modeling, business rules, and professional backend design practices.

This project is intentionally scoped to appointment management only, serving as a portfolio-quality example of real-world backend design using Spring Boot.

⸻

Scope

In Scope
•	Create, cancel, reschedule and complete appointments
•	Manage doctors and patients (soft delete)
•	Doctor agenda and patient appointment history
•	Appointment audit trail (event-based)
•	Doctor specialties with advanced many-to-many modeling

Out of Scope
•	Billing or payments
•	Prescriptions
•	Medical records
•	External integrations
•	Patient clinical data

⸻

Actors

The system is designed around the following actors (conceptual, not security roles):
•	Staff / Admin
Manages doctors, patients, and appointments.
•	Doctor
Views agenda and completes appointments.
•	Patient
Views appointment history.

⸻

Core Features
•	Appointment scheduling with business rule validation
•	Fixed appointment duration
•	Doctor availability validation (no overlapping appointments)
•	Patient overlapping appointments allowed
•	Soft delete for doctors and patients
•	Explicit appointment lifecycle (SCHEDULED, CANCELLED, COMPLETED)
•	Automatic audit event generation
•	Use-case driven (vertical slice) design

⸻

Appointment Lifecycle

Appointments follow a clear lifecycle:
•	SCHEDULED
•	CANCELLED
•	COMPLETED

Every meaningful change generates an audit event:
•	CREATED
•	CANCELLED
•	RESCHEDULED
•	COMPLETED

Audit events provide traceability (what happened, when, and by whom).

⸻

API Overview

Doctor & Patient Management
•	Create, retrieve, update and soft-delete doctors
•	Create, retrieve, update and soft-delete patients

Appointments
•	Create appointment
•	Cancel appointment
•	Reschedule appointment
•	Complete appointment
•	Doctor agenda by date
•	Patient appointment history
•	Appointment audit trail

⸻

Domain Design Highlights
•	Clear separation between use cases and domain events
•	Explicit domain modeling (Appointment, AuditEvent, Specialty)
•	Many-to-many relationship implemented via join entity (DoctorSpecialty)
•	Business rules enforced at the service layer
•	Designed for readability, maintainability, and extensibility

⸻

Design Documentation

Detailed design decisions, business rules, and entity relationships are documented separately in the Hospital Design Specification.

⸻

Purpose of This Project

This project was built to practice and demonstrate:
•	Professional backend system design
•	Domain-driven thinking (without overengineering)
•	Clean API design
•	Realistic business rules
•	Auditability and traceability

It is intended as a portfolio project, not a production-ready healthcare system.

How to Run
mvn spring-boot:run

Final Notes

This project prioritizes clarity of design over feature count.
The goal is to show how a real-world backend system is thought through before being coded.
