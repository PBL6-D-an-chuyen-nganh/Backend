package com.pbl.backend.controller;

import com.pbl.backend.dto.ScheduleRequestDTO;
import com.pbl.backend.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    @PostMapping
    public ResponseEntity<String> saveDoctorSchedule(@RequestBody ScheduleRequestDTO request) {
        try {
            scheduleService.createOrUpdateSchedule(request);
            return ResponseEntity.ok("Đã lưu lịch làm việc thành công.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}