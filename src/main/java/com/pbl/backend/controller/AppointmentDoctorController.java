package com.pbl.backend.controller;

import com.pbl.backend.dto.response.AppointmentListResponseDTO;
import com.pbl.backend.dto.request.AppointmentRequestDTO;
import com.pbl.backend.model.Appointment;
import com.pbl.backend.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/doctor/appointments")
@RequiredArgsConstructor
public class AppointmentDoctorController {

    private final AppointmentService appointmentService;

    @PostMapping("/create")
    public ResponseEntity<?> createAppointment(@Valid @RequestBody AppointmentRequestDTO request) {
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
    public ResponseEntity<String> deleteAppointmentByDoctor(@PathVariable Long appointmentId) {
        try {
            appointmentService.deleteAppointmentByDoctor(appointmentId);
            return ResponseEntity.ok("Đã xoá tất cả lịch hẹn của bác sĩ với ID: " + appointmentId + " thành công.");
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{doctorId}")
    public ResponseEntity<AppointmentListResponseDTO> getAppointmentsByDoctorIdAndDate(
            @PathVariable Long doctorId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        AppointmentListResponseDTO response = appointmentService.getAppointmentsByDoctorIdAndDate(doctorId, date);
        return ResponseEntity.ok(response);
    }

}