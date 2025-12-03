package com.pbl.backend.controller;

import com.pbl.backend.dto.response.DoctorDTO;
import com.pbl.backend.dto.response.DoctorSummaryDTO;
import com.pbl.backend.dto.response.PagedResponse;
import com.pbl.backend.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/doctors")
@RequiredArgsConstructor
public class DoctorController {

    private final DoctorService doctorService;

    @GetMapping
    public ResponseEntity<PagedResponse<DoctorDTO>> getDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "userId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<DoctorDTO> doctors = doctorService.getDoctors(pageable);

        PagedResponse<DoctorDTO> response = new PagedResponse<>(
                doctors.getContent(),
                doctors.getNumber(),
                doctors.getSize(),
                doctors.getTotalElements(),
                doctors.getTotalPages(),
                doctors.isLast()
        );

        return ResponseEntity.ok(response);
    }
    @GetMapping("/{id}")
    public ResponseEntity<DoctorDTO> getDoctorById(@PathVariable Long id) {
        DoctorDTO doctor = doctorService.getDoctorById(id);
        if (doctor == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(doctor);
    }

    @GetMapping("/search-filter")
    public ResponseEntity<PagedResponse<DoctorDTO>> searchDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "userId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,

            @RequestParam(required = false) String name,
            @RequestParam(required = false) String degree,
            @RequestParam(required = false) String position
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<DoctorDTO> doctors = doctorService.searchDoctors(name, degree, position, pageable);

        PagedResponse<DoctorDTO> response = new PagedResponse<>(
                doctors.getContent(),
                doctors.getNumber(),
                doctors.getSize(),
                doctors.getTotalElements(),
                doctors.getTotalPages(),
                doctors.isLast()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/by-specialty")
    public ResponseEntity<List<DoctorSummaryDTO>> getDoctorsBySpecialty(@RequestParam Integer specialtyId) {
        List<DoctorSummaryDTO> doctors = doctorService.getDoctorsBySpecialty(specialtyId);
        return ResponseEntity.ok(doctors);
    }

    @GetMapping("/{doctorId}/available-slots")
    public ResponseEntity<Map<LocalDate, List<LocalTime>>> getAvailableSlotsForDoctor(@PathVariable Long doctorId) {
        Map<LocalDate, List<LocalTime>> availableSlots = doctorService.getAvailableSlotsForDoctor(doctorId);
        return ResponseEntity.ok(availableSlots);
    }
}