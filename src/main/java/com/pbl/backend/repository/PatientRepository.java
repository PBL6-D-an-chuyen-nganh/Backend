package com.pbl.backend.repository;

import com.pbl.backend.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Patient findByPatientId(Long patientId);
}
