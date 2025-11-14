package com.pbl.backend.controller;

import com.pbl.backend.dto.AppointmentInfoForDiagDTO;
import com.pbl.backend.dto.AppointmentListResponseDTO;
import com.pbl.backend.dto.AppointmentRequestDTO;
import com.pbl.backend.model.Appointment;
import com.pbl.backend.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

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

    @DeleteMapping("/by-doctor/{appointmentId}")
    public ResponseEntity<String> deleteAppointmentByDoctor(@PathVariable Long appointmentId) {
        try {
            appointmentService.deleteAppointmentByDoctor(appointmentId);
            return ResponseEntity.ok("Đã xoá tất cả lịch hẹn của bác sĩ với ID: " + appointmentId + " thành công.");
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<AppointmentListResponseDTO> getAppointmentsByDoctorIdAndDate(
            @PathVariable Long doctorId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        AppointmentListResponseDTO response = appointmentService.getAppointmentsByDoctorIdAndDate(doctorId, date);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{appointmentId}")
    public ResponseEntity<AppointmentInfoForDiagDTO> getAppointmentById(@PathVariable Long appointmentId) {
        AppointmentInfoForDiagDTO appointment = appointmentService.getAppointmentById(appointmentId);
        return ResponseEntity.ok(appointment);
    }

}