package com.pbl.backend.controller;

import com.pbl.backend.dto.request.DoctorCreateRequestDTO;
import com.pbl.backend.dto.request.DoctorEditRequestDTO;
import com.pbl.backend.dto.response.*;
import com.pbl.backend.service.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/doctors")
@RequiredArgsConstructor
public class DoctorAdminController {

    private final DoctorService doctorService;

    @GetMapping
    public ResponseEntity<PagedResponse<DoctorSummaryDTO>> getDoctors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "userId") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<DoctorSummaryDTO> doctors = doctorService.getDoctorSummaries(pageable);

        PagedResponse<DoctorSummaryDTO> response = new PagedResponse<>(
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
    public ResponseEntity<PagedResponse<DoctorSummaryDTO>> searchDoctors(
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

        Page<DoctorSummaryDTO> doctors = doctorService.searchDoctorSummaries(name, degree, position, pageable);

        PagedResponse<DoctorSummaryDTO> response = new PagedResponse<>(
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

    @PostMapping
    public ResponseEntity<DoctorDTO> createDoctor(@RequestBody DoctorCreateRequestDTO request) {
        DoctorDTO newDoctor = doctorService.createDoctor(request);
        return ResponseEntity.ok(newDoctor);
    }

    @GetMapping("/{id}/edit-info")
    public ResponseEntity<DoctorEditResponseDTO> getDoctorEditInfo(@PathVariable Long id) {
        DoctorEditResponseDTO doctorInfo = doctorService.getDoctorForEdit(id);
        return ResponseEntity.ok(doctorInfo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DoctorDTO> updateDoctor(
            @PathVariable Long id,
            @RequestBody DoctorEditRequestDTO request) {
        DoctorDTO updatedDoctor = doctorService.updateDoctor(id, request);
        return ResponseEntity.ok(updatedDoctor);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reopen-doctor")
    public ResponseEntity<DoctorDTO> reopenDoctorAccount(@PathVariable Long id) {
        DoctorDTO reopenedDoctor = doctorService.reopenDoctorAccount(id);
        return ResponseEntity.ok(reopenedDoctor);
    }
}