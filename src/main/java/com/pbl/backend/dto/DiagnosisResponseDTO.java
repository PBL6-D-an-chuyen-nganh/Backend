// DiagnosisResponseDTO.java
package com.pbl.backend.dto;

import com.pbl.backend.model.Diagnosis;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DiagnosisResponseDTO {
    private Long diagnosisID;
    private Long recordID;
    private Long doctorUserId;
    private Long patientId;
    private String disease;
    private LocalDate dateOfDiagnosis;
    private String doctorNotes;

    public DiagnosisResponseDTO(Diagnosis diagnosis) {
        this.diagnosisID = diagnosis.getDiagnosisID();
        this.recordID = diagnosis.getMedicalRecord().getRecordID();

        if (diagnosis.getDoctor() != null) {
            this.doctorUserId = diagnosis.getDoctor().getUserId();
        }

        if (diagnosis.getMedicalRecord().getPatient() != null) {
            this.patientId = diagnosis.getMedicalRecord().getPatient().getPatientId();
        }

        this.disease = diagnosis.getDisease();
        this.dateOfDiagnosis = diagnosis.getDateOfDiagnosis();
        this.doctorNotes = diagnosis.getDoctorNotes();
    }
}