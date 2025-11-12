package com.pbl.backend.dto;

import com.pbl.backend.model.Diagnosis;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DiagnosisListDTO {

    private Long patientId;
    private String patientName;
    private String gender;
    private LocalDate dateOfDiagnosis;


    public DiagnosisListDTO(Diagnosis diagnosis) {

        if (diagnosis.getMedicalRecord().getPatient() != null) {
            this.patientId = diagnosis.getMedicalRecord().getPatient().getPatientId();
            this.patientName = diagnosis.getMedicalRecord().getPatient().getName();
            this.gender = diagnosis.getMedicalRecord().getPatient().getGender();
        }

        this.dateOfDiagnosis = diagnosis.getDateOfDiagnosis();
    }

}