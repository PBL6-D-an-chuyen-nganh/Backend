package com.pbl.backend.controller;

import com.pbl.backend.dto.AppointmentRequestDTO;
import com.pbl.backend.model.Appointment;
import com.pbl.backend.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;

    // API 3: Tạo appointment với thông tin bao gồm cả doctorId
    @PostMapping("/create")
    public ResponseEntity<?> createAppointment(@RequestBody AppointmentRequestDTO request) {
        try {
            Appointment newAppointment = appointmentService.createAppointment(request);
            return new ResponseEntity<>(newAppointment, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}