package com.pbl.backend.controller;

import com.pbl.backend.dto.response.DiagnosisResponseDTO;
import com.pbl.backend.service.DiagnosisService;
import com.pbl.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/user/diagnoses")
@RequiredArgsConstructor
public class DiagnosisUserController {

    private final DiagnosisService diagnosisService;

    private final UserService userService;

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

    @GetMapping("/by-user")
    public ResponseEntity<List<DiagnosisResponseDTO>> getFamilyDiagnoses() {
        Long currentUserId = userService.getCurrentUserId();

        List<DiagnosisResponseDTO> list = diagnosisService.getAllDiagnosesManagedByUser(currentUserId);
        return ResponseEntity.ok(list);
    }

}