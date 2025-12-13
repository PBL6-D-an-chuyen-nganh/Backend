package com.pbl.backend.controller;

import com.pbl.backend.dto.request.DoctorProfileUpdateRequest;
import com.pbl.backend.dto.response.DoctorDTO;
import com.pbl.backend.dto.response.DoctorProfileUpdateResponseDTO;
import com.pbl.backend.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctor/profile")
@RequiredArgsConstructor
public class ProfileDoctorController {

    private final DoctorService doctorService;


    @GetMapping()
    public ResponseEntity<DoctorProfileUpdateResponseDTO> getMyDoctorProfile() {
        return ResponseEntity.ok(doctorService.getCurrentDoctorProfile());
    }

    @PutMapping("/update")
    public ResponseEntity<DoctorDTO> updateDoctorProfile(@RequestBody DoctorProfileUpdateRequest request) {
        return ResponseEntity.ok(doctorService.updateCurrentDoctorProfile(request));
    }
}