package com.pbl.backend.dto;

import com.pbl.backend.model.Diagnosis;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientListDTO {
    private String name;
    private String disease;
    private LocalDate dateOfDiagnosis;
    private Long patientId;

    public PatientListDTO(Diagnosis diagnosis) {
        if (diagnosis.getMedicalRecord().getPatient() != null) {
            this.patientId = diagnosis.getMedicalRecord().getPatient().getPatientId();
            this.name = diagnosis.getMedicalRecord().getPatient().getName();
        }

        this.disease = diagnosis.getDisease();
        this.dateOfDiagnosis = diagnosis.getDateOfDiagnosis();
    }
}