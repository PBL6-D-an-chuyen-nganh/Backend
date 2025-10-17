package com.pbl.backend.service;

import com.pbl.backend.dto.AppointmentRequestDTO;
import com.pbl.backend.model.Appointment;
import com.pbl.backend.model.Doctor;
import com.pbl.backend.model.Patient;
import com.pbl.backend.repository.AppointmentRepository;
import com.pbl.backend.repository.DoctorRepository;
import com.pbl.backend.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorService doctorService;

    @Transactional
    public Appointment createAppointment(AppointmentRequestDTO request) {
        Long doctorId = request.getDoctorId();
        LocalDateTime requestedTime = request.getTime();
        Long creatorId = request.getCreatorId();

        if (doctorId == null || requestedTime == null) {
            throw new RuntimeException("Thiếu thông tin bác sĩ hoặc thời gian hẹn.");
        }

        Doctor selectedDoctor = doctorRepository.findByUserId(doctorId)
                .orElseThrow(() -> new RuntimeException("Bác sĩ với UserID " + doctorId + " không tồn tại."));

        Map<LocalDate, List<LocalTime>> availableSlots = doctorService.getAvailableSlotsForDoctor(doctorId);
        LocalDate requestedDate = requestedTime.toLocalDate();
        LocalTime requestedTimePart = requestedTime.toLocalTime();
        List<LocalTime> validTimesForDay = availableSlots.get(requestedDate);
        if (validTimesForDay == null || !validTimesForDay.contains(requestedTimePart)) {
            throw new RuntimeException("Khung giờ bạn chọn không hợp lệ hoặc đã có người khác đặt.");
        }

        Optional<Appointment> existingAppointment = appointmentRepository.findByDoctorUserIdAndTime(doctorId, requestedTime);
        if (existingAppointment.isPresent()) {
            throw new RuntimeException("Lỗi: Bác sĩ đã có lịch hẹn vào thời điểm này. Vui lòng chọn giờ khác.");
        }

        Patient newPatient = new Patient();
        newPatient.setName(request.getPatientInfo().getName());
        newPatient.setEmail(request.getPatientInfo().getEmail());
        newPatient.setPhoneNumber(request.getPatientInfo().getPhoneNumber());
        newPatient.setGender(request.getPatientInfo().getGender());
        newPatient.setDateOfBirth(request.getPatientInfo().getDateOfBirth());
        Patient savedPatient = patientRepository.save(newPatient);

        Appointment newAppointment = new Appointment();
        newAppointment.setPatient(savedPatient);
        newAppointment.setDoctor(selectedDoctor);
        newAppointment.setTime(requestedTime);
        newAppointment.setNote(request.getNote());
        newAppointment.setCreatorId(creatorId);

        return appointmentRepository.save(newAppointment);
    }
}