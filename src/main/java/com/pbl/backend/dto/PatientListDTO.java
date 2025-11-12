package com.pbl.backend.dto;

import com.pbl.backend.model.Diagnosis;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PatientListDTO {
    private String name;
    private String gender;
    private LocalDate dateOfDiagnosis;
    private Long patientId;

    public PatientListDTO(Diagnosis diagnosis) {
        if (diagnosis.getMedicalRecord().getPatient() != null) {
            this.patientId = diagnosis.getMedicalRecord().getPatient().getPatientId();
            this.name = diagnosis.getMedicalRecord().getPatient().getName();
            this.gender = diagnosis.getMedicalRecord().getPatient().getGender();
        }
        this.dateOfDiagnosis = diagnosis.getDateOfDiagnosis();
    }
}