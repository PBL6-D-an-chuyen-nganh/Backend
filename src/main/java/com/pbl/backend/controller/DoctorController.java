package com.pbl.backend.controller;

import com.pbl.backend.model.Doctor;
import com.pbl.backend.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    // API 1: Lấy danh sách bác sĩ theo chuyên khoa
    @GetMapping("/by-specialty")
    public ResponseEntity<List<Doctor>> getDoctorsBySpecialty(@RequestParam Integer specialtyId) {
        List<Doctor> doctors = doctorService.getDoctorsBySpecialty(specialtyId);
        return ResponseEntity.ok(doctors);
    }

    // API 2: Lấy lịch rảnh của một bác sĩ cụ thể
    @GetMapping("/{doctorId}/available-slots")
    public ResponseEntity<List<LocalDateTime>> getAvailableSlotsForDoctor(@PathVariable Long doctorId) {
        List<LocalDateTime> availableSlots = doctorService.getAvailableSlotsForDoctor(doctorId);
        return ResponseEntity.ok(availableSlots);
    }
}