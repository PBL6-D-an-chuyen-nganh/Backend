package com.pbl.backend.controller;

import com.pbl.backend.dto.DoctorDTO;
import com.pbl.backend.dto.PagedResponse;
import com.pbl.backend.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

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
    public ResponseEntity<DoctorDTO> getDoctorById(@PathVariable Integer id) {
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

}
