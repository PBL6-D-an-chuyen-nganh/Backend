package com.pbl.backend.service;

import com.pbl.backend.dto.response.DiagnosisListDTO;
import com.pbl.backend.dto.request.DiagnosisRequestDTO;
import com.pbl.backend.dto.response.DiagnosisResponseDTO;
import com.pbl.backend.dto.response.PatientListDTO;
import com.pbl.backend.model.Diagnosis;
import com.pbl.backend.model.Doctor;
import com.pbl.backend.model.MedicalRecord;
import com.pbl.backend.model.Patient;
import com.pbl.backend.repository.DiagnosisRepository;
import com.pbl.backend.repository.DoctorRepository;
import com.pbl.backend.repository.MedicalRecordRepository;
import com.pbl.backend.repository.PatientRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiagnosisService {

    @Autowired
    private DiagnosisRepository diagnosisRepository;
    @Autowired
    private MedicalRecordRepository medicalRecordRepository;
    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Transactional
    public DiagnosisResponseDTO createDiagnosis(DiagnosisRequestDTO requestDTO) {
        Doctor doctor = doctorRepository.findById(requestDTO.getDoctorUserId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Doctor với User ID: " + requestDTO.getDoctorUserId()));

        Patient patient = patientRepository.findById(requestDTO.getPatientId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Patient với ID: " + requestDTO.getPatientId()));

        MedicalRecord medicalRecord = patient.getMedicalRecord();

        if (medicalRecord == null) {
            MedicalRecord newRecord = new MedicalRecord();
            medicalRecord = medicalRecordRepository.save(newRecord);

            patient.setMedicalRecord(medicalRecord);
            patientRepository.save(patient);
        }

        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setMedicalRecord(medicalRecord);
        diagnosis.setDoctor(doctor);
        diagnosis.setDisease(requestDTO.getDisease());
        diagnosis.setDateOfDiagnosis(requestDTO.getDateOfDiagnosis());
        diagnosis.setDoctorNotes(requestDTO.getDoctorNotes());
        diagnosis.setTreatmentPlan(requestDTO.getTreatmentPlan());

        Diagnosis savedDiagnosis = diagnosisRepository.save(diagnosis);

        if (savedDiagnosis.getMedicalRecord().getPatient() == null) {
            savedDiagnosis.getMedicalRecord().setPatient(patient);
        }

        return new DiagnosisResponseDTO(savedDiagnosis);
    }

    @Transactional(readOnly = true)
    public List<DiagnosisResponseDTO> getDiagnosesByPatientId(Long patientId) {
        MedicalRecord medicalRecord = medicalRecordRepository.findByPatient_PatientId(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy MedicalRecord cho Patient ID: " + patientId));

        List<Diagnosis> diagnoses = medicalRecord.getDiagnoses();

        return diagnoses.stream()
                .map(DiagnosisResponseDTO::new)
                .collect(Collectors.toList());
    }

    public DiagnosisResponseDTO getDiagnosisById(Long diagnosisId) {
        Diagnosis diagnosis = diagnosisRepository.findById(diagnosisId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Diagnosis với ID: " + diagnosisId));

        return new DiagnosisResponseDTO(diagnosis);
    }

    @Transactional(readOnly = true)
    public List<PatientListDTO> getPatientListByDoctorId(Long doctorUserId) {
        List<Diagnosis> diagnoses = diagnosisRepository.findAllByDoctorUserIdWithDetails(doctorUserId);
        return diagnoses.stream()
                .map(PatientListDTO::new)
                .collect(Collectors.toList());
    }

    public List<DiagnosisListDTO> getDiagnosesByDoctorIdAndDate(Long doctorUserId, LocalDate date) {
        List<Diagnosis> diagnoses = diagnosisRepository.findByDoctorUserIdAndDateOfDiagnosis(doctorUserId, date);

        return diagnoses.stream()
                .map(DiagnosisListDTO::new)
                .collect(Collectors.toList());
    }
}