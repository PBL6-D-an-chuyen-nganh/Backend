package com.pbl.backend.controller;

import com.pbl.backend.dto.DiagnosisResponseDTO;
import com.pbl.backend.service.DiagnosisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user/diagnoses")
public class DiagnosisUserController {

    @Autowired
    private DiagnosisService diagnosisService;

    @GetMapping("/{patientId}")
    public ResponseEntity<List<DiagnosisResponseDTO>> getDiagnosesByPatientId(@PathVariable Long patientId) {
        List<DiagnosisResponseDTO> diagnoses = diagnosisService.getDiagnosesByPatientId(patientId);
        return ResponseEntity.ok(diagnoses);
    }

    @GetMapping("/{diagnosisId}/detail")
    public ResponseEntity<DiagnosisResponseDTO> getDiagnosisById(@PathVariable Long diagnosisId) {
        DiagnosisResponseDTO diagnosis = diagnosisService.getDiagnosisById(diagnosisId);
        return ResponseEntity.ok(diagnosis);
    }

}