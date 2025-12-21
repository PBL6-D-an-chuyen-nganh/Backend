package com.pbl.backend.dto.response;

import com.pbl.backend.model.Diagnosis;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DiagnosisListDTO {
    private Long patientId;
    private String name;
    private String gender;
    private LocalDate dateOfDiagnosis;


    public DiagnosisListDTO(Diagnosis diagnosis) {

        if (diagnosis.getMedicalRecord().getPatient() != null) {
            this.patientId = diagnosis.getMedicalRecord().getPatient().getPatientId();
            this.name = diagnosis.getMedicalRecord().getPatient().getName();
            this.gender = diagnosis.getMedicalRecord().getPatient().getGender();
        }

        this.dateOfDiagnosis = diagnosis.getDateOfDiagnosis();
    }

}