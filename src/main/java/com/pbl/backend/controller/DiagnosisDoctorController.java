package com.pbl.backend.controller;

import com.pbl.backend.dto.request.DiagnosisRequestDTO;
import com.pbl.backend.dto.response.DiagnosisListDTO;
import com.pbl.backend.dto.response.DiagnosisResponseDTO;
import com.pbl.backend.dto.response.PatientListDTO;
import com.pbl.backend.service.DiagnosisService;
import com.pbl.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctor/diagnoses")
@RequiredArgsConstructor
@Slf4j
public class DiagnosisDoctorController {


    private final DiagnosisService diagnosisService;
    private final UserService userService;

    @PostMapping("/create")
    public ResponseEntity<DiagnosisResponseDTO> createDiagnosis(@RequestBody DiagnosisRequestDTO requestDTO) {
        DiagnosisResponseDTO createdDiagnosis = diagnosisService.createDiagnosis(requestDTO);
        return new ResponseEntity<>(createdDiagnosis, HttpStatus.CREATED);
    }

    @GetMapping("/{diagnosisId}")
    public ResponseEntity<DiagnosisResponseDTO> getDiagnosisById(@PathVariable Long diagnosisId) {
        DiagnosisResponseDTO diagnosis = diagnosisService.getDiagnosisById(diagnosisId);
        return ResponseEntity.ok(diagnosis);
    }

    @GetMapping("/patient-list")
    public ResponseEntity<List<PatientListDTO>> getPatientListByDoctorId() {
        Long doctorUserId = userService.getCurrentUserId();
        List<PatientListDTO> patientList = diagnosisService.getPatientListByDoctorId(doctorUserId);
        return ResponseEntity.ok(patientList);
    }

    @GetMapping("/diagnosis-list")
    public ResponseEntity<List<DiagnosisListDTO>> getDiagnosesByDoctorAndDate(
            @RequestParam(required = false) LocalDate date) {

        log.info("Received request to get diagnoses list. Date param: {}", date);

        if (date == null) {
            date = LocalDate.now();
        }
        Long doctorUserId = userService.getCurrentUserId();
        List<DiagnosisListDTO> diagnosesList = diagnosisService.getDiagnosesByDoctorIdAndDate(doctorUserId, date);
        if (!diagnosesList.isEmpty()) {
            log.info("Found {} diagnoses. First diagnosis date: {}",
                    diagnosesList.size(),
                    diagnosesList.get(0).getDateOfDiagnosis());
        } else {
            log.info("No diagnoses found for doctor ID: {} on date: {}", doctorUserId, date);
        }
        return ResponseEntity.ok(diagnosesList);
    }

}