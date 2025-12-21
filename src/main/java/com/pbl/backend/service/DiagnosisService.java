package com.pbl.backend.service;

import com.pbl.backend.dto.request.DiagnosisRequestDTO;
import com.pbl.backend.dto.response.DiagnosisListDTO;
import com.pbl.backend.dto.response.DiagnosisResponseDTO;
import com.pbl.backend.dto.response.PatientListDTO;
import com.pbl.backend.model.*;
import com.pbl.backend.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiagnosisService {

    private final DiagnosisRepository diagnosisRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional
    @CacheEvict(value = {
            "diagnoses",
            "patient_lists",
            "appointments_by_creator",
            "appointments_by_doctor",
            "appointment_details"
    }, allEntries = true)
    public DiagnosisResponseDTO createDiagnosis(DiagnosisRequestDTO requestDTO) {
        Doctor doctor = doctorRepository.findById(requestDTO.getDoctorUserId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Doctor với User ID: " + requestDTO.getDoctorUserId()));

        Patient patient = patientRepository.findById(requestDTO.getPatientId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Patient với ID: " + requestDTO.getPatientId()));

        if (requestDTO.getAppointmentId() == null) {
            throw new IllegalArgumentException("Appointment ID không được để trống khi tạo chẩn đoán.");
        }

        Appointment appointment = appointmentRepository.findById(requestDTO.getAppointmentId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Appointment với ID: " + requestDTO.getAppointmentId()));

        if (appointment.getDiagnosis() != null) {
            throw new RuntimeException("Lịch hẹn này đã có chẩn đoán rồi.");
        }

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

        diagnosis.setAppointment(appointment);

        appointment.setStatus("completed");
        appointmentRepository.save(appointment);

        Diagnosis savedDiagnosis = diagnosisRepository.save(diagnosis);

        if (savedDiagnosis.getMedicalRecord().getPatient() == null) {
            savedDiagnosis.getMedicalRecord().setPatient(patient);
        }

        return new DiagnosisResponseDTO(savedDiagnosis);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "diagnoses", key = "'patient_' + #patientId")
    public List<DiagnosisResponseDTO> getDiagnosesByPatientId(Long patientId) {
        MedicalRecord medicalRecord = medicalRecordRepository.findByPatient_PatientId(patientId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy MedicalRecord cho Patient ID: " + patientId));

        List<Diagnosis> diagnoses = medicalRecord.getDiagnoses();

        return diagnoses.stream()
                .map(DiagnosisResponseDTO::new)
                .collect(Collectors.toList());
    }
    @Cacheable(value = "diagnoses", key = "'diagnosis_' + #diagnosisId")
    public DiagnosisResponseDTO getDiagnosisById(Long diagnosisId) {
        Diagnosis diagnosis = diagnosisRepository.findById(diagnosisId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy Diagnosis với ID: " + diagnosisId));
        return new DiagnosisResponseDTO(diagnosis);
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "patient_lists", key = "'doctor_' + #doctorUserId")
    public List<PatientListDTO> getPatientListByDoctorId(Long doctorUserId) {
        List<Diagnosis> diagnoses = diagnosisRepository.findAllByDoctorUserIdWithDetails(doctorUserId);
        return diagnoses.stream()
                .map(PatientListDTO::new)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "diagnoses", key = "'doctor_' + #doctorUserId + '_date_' + #date.toString()")
    public List<DiagnosisListDTO> getDiagnosesByDoctorIdAndDate(Long doctorUserId, LocalDate date) {
        List<Diagnosis> diagnoses = diagnosisRepository.findByDoctorUserIdAndDateOfDiagnosis(doctorUserId, date);

        return diagnoses.stream()
                .map(DiagnosisListDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "diagnoses", key = "'manager_' + #userId")
    public List<DiagnosisResponseDTO> getAllDiagnosesManagedByUser(Long userId) {
        List<Diagnosis> diagnoses = diagnosisRepository.findAllDiagnosesByManagerUserId(userId);

        return diagnoses.stream()
                .map(DiagnosisResponseDTO::new)
                .collect(Collectors.toList());
    }
}