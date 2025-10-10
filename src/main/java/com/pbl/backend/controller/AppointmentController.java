package com.pbl.backend.controller;

import com.pbl.backend.dto.AppointmentRequestDTO;
import com.pbl.backend.model.Appointment;
import com.pbl.backend.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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

    @GetMapping("/unavailable-dates-in-month")
    public ResponseEntity<List<LocalDate>> getUnavailableDatesInMonth(
            @RequestParam Integer specialtyId) {
        List<LocalDate> unavailableDates = appointmentService.findUnavailableDatesInMonth(specialtyId);
        return ResponseEntity.ok(unavailableDates);
    }

    @GetMapping("/unavailable-slots-by-specialty")
    public ResponseEntity<Map<Integer, List<LocalDateTime>>> getUnavailableSlotsBySpecialtyForDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Map<Integer, List<LocalDateTime>> result = appointmentService.findUnavailableSlotsBySpecialtyForDate(date);
        return ResponseEntity.ok(result);
    }
}