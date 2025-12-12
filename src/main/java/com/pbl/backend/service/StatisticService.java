package com.pbl.backend.service;

import com.pbl.backend.dto.response.AppointmentStatsDTO;
import com.pbl.backend.dto.response.UserCancellationStatsDTO;
import com.pbl.backend.repository.AppointmentRepository;
import com.pbl.backend.repository.CancellationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticService {

    private final CancellationLogRepository cancellationLogRepository;

    public List<UserCancellationStatsDTO> getUserCancellationStats() {
        return cancellationLogRepository.getCancellationStatistics();
    }

    private final AppointmentRepository appointmentRepository;

    public List<AppointmentStatsDTO> getMonthlyAppointmentStatistics() {
        YearMonth currentMonth = YearMonth.now();
        YearMonth startMonth = currentMonth.minusMonths(11);

        LocalDateTime startDate = startMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = currentMonth.atEndOfMonth().atTime(23, 59, 59);

        List<AppointmentStatsDTO> rawStats = appointmentRepository.countAppointmentsByMonth(startDate, endDate);

        Map<String, Long> statsMap = rawStats.stream()
                .collect(Collectors.toMap(
                        stats -> stats.getYear() + "-" + stats.getMonth(),
                        AppointmentStatsDTO::getCount
                ));

        List<AppointmentStatsDTO> finalStats = new ArrayList<>();
        YearMonth iteratorMonth = startMonth;

        while (!iteratorMonth.isAfter(currentMonth)) {
            String key = iteratorMonth.getYear() + "-" + iteratorMonth.getMonthValue();

            long count = statsMap.getOrDefault(key, 0L);

            finalStats.add(new AppointmentStatsDTO(
                    iteratorMonth.getMonthValue(),
                    iteratorMonth.getYear(),
                    count
            ));

            iteratorMonth = iteratorMonth.plusMonths(1);
        }

        return finalStats;
    }
}