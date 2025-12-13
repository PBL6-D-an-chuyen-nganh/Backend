package com.pbl.backend.controller;

import com.pbl.backend.dto.request.ChangePasswordRequest;
import com.pbl.backend.dto.request.DoctorProfileUpdateRequest;
import com.pbl.backend.dto.response.DoctorDTO;
import com.pbl.backend.dto.response.DoctorProfileUpdateResponseDTO;
import com.pbl.backend.service.DoctorService;
import com.pbl.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctor/profile")
@RequiredArgsConstructor
public class ProfileDoctorController {

    private final DoctorService doctorService;
    private final UserService userService;


    @GetMapping()
    public ResponseEntity<DoctorProfileUpdateResponseDTO> getMyDoctorProfile() {
        return ResponseEntity.ok(doctorService.getCurrentDoctorProfile());
    }

    @PutMapping("/update")
    public ResponseEntity<DoctorDTO> updateDoctorProfile(@RequestBody DoctorProfileUpdateRequest request) {
        return ResponseEntity.ok(doctorService.updateCurrentDoctorProfile(request));
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@RequestBody ChangePasswordRequest request) {
        userService.changePassword(request);
        return ResponseEntity.ok("Đổi mật khẩu thành công.");
    }
}