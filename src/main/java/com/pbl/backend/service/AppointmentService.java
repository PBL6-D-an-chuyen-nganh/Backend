package com.pbl.backend.service;

import com.pbl.backend.dto.*;
import com.pbl.backend.model.Appointment;
import com.pbl.backend.model.Doctor;
import com.pbl.backend.model.Patient;
import com.pbl.backend.repository.AppointmentRepository;
import com.pbl.backend.repository.DoctorRepository;
import com.pbl.backend.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorService doctorService;
    private final EmailService emailService;

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

        LocalDateTime now = LocalDateTime.now();

        long hoursUntilAppointment = Duration.between(now, requestedTime).toHours();

        newAppointment.setStatus("active");

        newAppointment.setCreatedAt(LocalDateTime.now());
        newAppointment.setCreatorId(creatorId);

        Appointment savedAppointment = appointmentRepository.save(newAppointment);

        try {
            emailService.sendAppointmentConfirmationEmail(savedAppointment);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return savedAppointment;
    }

    public AppointmentListResponseDTO getAppointmentsByCreatorId(Long creatorId) {
        List<Appointment> appointmentEntities = appointmentRepository.findByCreatorId(creatorId);

        List<AppointmentDetailDTO> appointmentDTOs = appointmentEntities
                .stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());

        long totalAppointments = appointmentEntities.size();

        return new AppointmentListResponseDTO(totalAppointments, appointmentDTOs);
    }

    private AppointmentDetailDTO convertToDetailDTO(Appointment appointment) {
        Doctor doctor = appointment.getDoctor();
        Patient patient = appointment.getPatient();

        DoctorSummaryDTO doctorDTO = new DoctorSummaryDTO();
        doctorDTO.setUserId(doctor.getUserId());
        doctorDTO.setName(doctor.getName());
        doctorDTO.setPosition(doctor.getPosition());
        doctorDTO.setDegree(doctor.getDegree());

        PatientSummaryDTO patientDTO = new PatientSummaryDTO();
        patientDTO.setName(patient.getName());
        patientDTO.setEmail(patient.getEmail());
        patientDTO.setPhoneNumber(patient.getPhoneNumber());

        AppointmentDetailDTO appointmentDTO = new AppointmentDetailDTO();
        appointmentDTO.setAppointmentID(appointment.getAppointmentID());
        appointmentDTO.setTime(appointment.getTime());
        appointmentDTO.setStatus(appointment.getStatus());
        appointmentDTO.setNote(appointment.getNote());
        appointmentDTO.setCreatorId(appointment.getCreatorId());
        appointmentDTO.setCreatedAt(appointment.getCreatedAt());

        appointmentDTO.setDoctor(doctorDTO);
        appointmentDTO.setPatientInfo(patientDTO);

        return appointmentDTO;
    }

    public AppointmentListResponseDTO getAppointmentsByDoctorIdAndDate(Long doctorId, LocalDate date) {
        if (date == null) {
            throw new RuntimeException("Vui lòng cung cấp ngày cụ thể.");
        }

        LocalDateTime startOfDay = date.atStartOfDay();

        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Appointment> appointmentEntities = appointmentRepository
                .findByDoctor_UserIdAndTimeBetween(doctorId, startOfDay, endOfDay);

        List<AppointmentDetailDTO> appointmentDTOs = appointmentEntities
                .stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());

        long totalAppointments = appointmentEntities.size();

        return new AppointmentListResponseDTO(totalAppointments, appointmentDTOs);
    }

    @Transactional
    public void deleteAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn với ID: " + appointmentId));
        try {
            emailService.sendAppointmentCancellationEmail(appointment);
        } catch (Exception e) {
            System.err.println("Sắp xoá lịch hẹn ID: " + appointmentId + " nhưng gửi email huỷ thất bại: " + e.getMessage());
        }
        appointmentRepository.delete(appointment);
    }
    @Transactional
    public void deleteAppointmentByDoctor(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn với ID: " + appointmentId));
        try {
            emailService.sendAppointmentDoctorCancellationEmail(appointment);
        } catch (Exception e) {
            System.err.println("Sắp xoá lịch hẹn ID: " + appointmentId + " nhưng gửi email huỷ thất bại: " + e.getMessage());
        }
        appointmentRepository.delete(appointment);
    }
}