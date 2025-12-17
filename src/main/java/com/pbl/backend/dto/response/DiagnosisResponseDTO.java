package com.pbl.backend.dto.response;

import com.pbl.backend.model.Diagnosis;
import com.pbl.backend.model.Patient;
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
    private String treatmentPlan;
    private Long appointmentId;

    public DiagnosisResponseDTO(Diagnosis diagnosis) {
        this.diagnosisID = diagnosis.getDiagnosisID();
        this.disease = diagnosis.getDisease();
        this.dateOfDiagnosis = diagnosis.getDateOfDiagnosis();
        this.doctorNotes = diagnosis.getDoctorNotes();
        this.treatmentPlan = diagnosis.getTreatmentPlan();

        if (diagnosis.getDoctor() != null) {
            this.doctorName = diagnosis.getDoctor().getName();
            this.specialty = diagnosis.getDoctor().getSpecialty();
            this.degree = diagnosis.getDoctor().getDegree();
        }

        if (diagnosis.getAppointment() != null) {
            this.appointmentId = diagnosis.getAppointment().getAppointmentID();
        }

        Patient foundPatient = null;

        if (diagnosis.getMedicalRecord() != null) {
            foundPatient = diagnosis.getMedicalRecord().getPatient();
        }

        if (foundPatient == null && diagnosis.getAppointment() != null) {
            foundPatient = diagnosis.getAppointment().getPatient();
        }

        if (foundPatient != null) {
            this.patientId = foundPatient.getPatientId();
            this.patientName = foundPatient.getName();
            this.gender = foundPatient.getGender();
        }
    }
}