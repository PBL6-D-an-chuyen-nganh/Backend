// DiagnosisController.java
package com.pbl.backend.controller;

import com.pbl.backend.dto.DiagnosisListDTO;
import com.pbl.backend.dto.DiagnosisRequestDTO;
import com.pbl.backend.dto.DiagnosisResponseDTO;
import com.pbl.backend.dto.PatientListDTO;
import com.pbl.backend.service.DiagnosisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class DiagnosisController {

    @Autowired
    private DiagnosisService diagnosisService;

    @PostMapping("/diagnoses")
    public ResponseEntity<DiagnosisResponseDTO> createDiagnosis(@RequestBody DiagnosisRequestDTO requestDTO) {
        DiagnosisResponseDTO createdDiagnosis = diagnosisService.createDiagnosis(requestDTO);
        return new ResponseEntity<>(createdDiagnosis, HttpStatus.CREATED);
    }

    @GetMapping("/patients/{patientId}/diagnoses")
    public ResponseEntity<List<DiagnosisResponseDTO>> getDiagnosesByPatientId(@PathVariable Long patientId) {
        List<DiagnosisResponseDTO> diagnoses = diagnosisService.getDiagnosesByPatientId(patientId);
        return ResponseEntity.ok(diagnoses);
    }

    @GetMapping("/diagnoses/{diagnosisId}")
    public ResponseEntity<DiagnosisResponseDTO> getDiagnosisById(@PathVariable Long diagnosisId) {
        DiagnosisResponseDTO diagnosis = diagnosisService.getDiagnosisById(diagnosisId);
        return ResponseEntity.ok(diagnosis);
    }

    @GetMapping("/doctors/{doctorUserId}/patient-list")
    public ResponseEntity<List<PatientListDTO>> getPatientListByDoctorId(@PathVariable Long doctorUserId) {
        List<PatientListDTO> patientList = diagnosisService.getPatientListByDoctorId(doctorUserId);
        return ResponseEntity.ok(patientList);
    }

    @GetMapping("/doctors/{doctorUserId}/diagnosis-list")
    public ResponseEntity<List<DiagnosisListDTO>> getDiagnosesByDoctorAndDate(
            @PathVariable Long doctorUserId,
            @RequestParam(required = false) LocalDate date) {

        if (date == null) {
            date = LocalDate.now();
        }

        List<DiagnosisListDTO> diagnosesList = diagnosisService.getDiagnosesByDoctorIdAndDate(doctorUserId, date);
        return ResponseEntity.ok(diagnosesList);
    }

}