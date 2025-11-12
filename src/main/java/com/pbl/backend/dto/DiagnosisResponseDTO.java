package com.pbl.backend.dto;

import com.pbl.backend.model.Diagnosis;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DiagnosisResponseDTO {
    private Long diagnosisID;
    private Long patientId;
    private String patientName;
    private String disease;
    private String gender;
    private LocalDate dateOfDiagnosis;
    private String doctorNotes;
    private String doctorName;
    private String specialty;
    private String degree;

    public DiagnosisResponseDTO(Diagnosis diagnosis) {
        this.diagnosisID = diagnosis.getDiagnosisID();

        if (diagnosis.getDoctor() != null) {
            this.doctorName = diagnosis.getDoctor().getName();
            this.specialty = diagnosis.getDoctor().getSpecialty();
            this.degree = diagnosis.getDoctor().getDegree();
        }

        if (diagnosis.getMedicalRecord().getPatient() != null) {
            this.patientId = diagnosis.getMedicalRecord().getPatient().getPatientId();
            this.patientName = diagnosis.getMedicalRecord().getPatient().getName();
            this.gender = diagnosis.getMedicalRecord().getPatient().getGender();
        }

        this.disease = diagnosis.getDisease();
        this.dateOfDiagnosis = diagnosis.getDateOfDiagnosis();
        this.doctorNotes = diagnosis.getDoctorNotes();
    }
}