package com.pbl.backend.scheduler;

import com.pbl.backend.model.Appointment;
import com.pbl.backend.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AppointmentScheduler {

    private final AppointmentRepository appointmentRepository;

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void updateAppointmentStatuses() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next24Hours = now.plusHours(24);

        List<Appointment> appointments = appointmentRepository.findByStatusAndTimeBetween(
                "active", now, next24Hours
        );

        for (Appointment appointment : appointments) {
            appointment.setStatus("inactive");
        }

        appointmentRepository.saveAll(appointments);
    }
}
