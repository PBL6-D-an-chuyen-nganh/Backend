package com.pbl.backend.controller;

import com.pbl.backend.dto.response.AppointmentStatsDTO;
import com.pbl.backend.dto.response.DoctorAppointmentStatsDTO;
import com.pbl.backend.dto.response.UserCancellationStatsDTO;
import com.pbl.backend.service.StatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/statistics")
@RequiredArgsConstructor
public class StatsAdminController {

    private final StatisticService statisticService;

    @GetMapping("/cancellations")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<UserCancellationStatsDTO>> getUserCancellationStats() {
        return ResponseEntity.ok(statisticService.getUserCancellationStats());
    }

    @GetMapping("/appointments")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<AppointmentStatsDTO>> getMonthlyAppointmentStats() {
        return ResponseEntity.ok(statisticService.getMonthlyAppointmentStatistics());
    }

    @GetMapping("/doctor-appointments")
    public ResponseEntity<List<DoctorAppointmentStatsDTO>> getDoctorStatsByMonth(
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "year", required = false) Integer year
    ) {
        if (month == null) {
            month = LocalDate.now().getMonthValue();
        }
        if (year == null) {
            year = LocalDate.now().getYear();
        }

        List<DoctorAppointmentStatsDTO> stats = statisticService.getDoctorAppointmentStats(month, year);
        return ResponseEntity.ok(stats);
    }
}