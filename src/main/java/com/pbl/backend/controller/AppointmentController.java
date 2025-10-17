package com.pbl.backend.controller;

import com.pbl.backend.dto.AppointmentListResponseDTO;
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

    @PostMapping("/create")
    public ResponseEntity<?> createAppointment(@RequestBody AppointmentRequestDTO request) {
        try {
            Appointment newAppointment = appointmentService.createAppointment(request);
            return new ResponseEntity<>(newAppointment, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<AppointmentListResponseDTO> getAppointmentsByCreatorId(@PathVariable Long creatorId) {
        AppointmentListResponseDTO response = appointmentService.getAppointmentsByCreatorId(creatorId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{appointmentId}")
    public ResponseEntity<String> deleteAppointment(@PathVariable Long appointmentId) {
        try {
            appointmentService.deleteAppointment(appointmentId);
            return ResponseEntity.ok("Đã xoá lịch hẹn với ID: " + appointmentId + " thành công.");
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}