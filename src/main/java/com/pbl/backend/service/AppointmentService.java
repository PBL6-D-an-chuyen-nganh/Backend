package com.pbl.backend.service;

import com.pbl.backend.dto.request.AppointmentRequestDTO;
import com.pbl.backend.dto.response.*;
import com.pbl.backend.model.*;
import com.pbl.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final UserRepository userRepository;
    private final CancellationLogRepository cancellationLogRepo;
    private final UserService userService;

    @Transactional
    @CacheEvict(value = {"appointments_by_creator", "appointments_by_doctor", "doctor_slots"}, allEntries = true)
    public Appointment createAppointment(AppointmentRequestDTO request) {
        Long doctorId = request.getDoctorId();
        LocalDateTime requestedTime = request.getTime();
        Long creatorId = request.getCreatorId();

        if (doctorId == null || requestedTime == null) {
            throw new RuntimeException("Thiếu thông tin bác sĩ hoặc thời gian hẹn.");
        }
        if (requestedTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Thời gian hẹn phải ở trong tương lai.");
        }
        if (creatorId == null) {
            throw new RuntimeException("Thiếu thông tin người tạo lịch hẹn (CreatorId).");
        }

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người tạo lịch hẹn (User ID: " + creatorId + ")."));


        Doctor selectedDoctor = doctorRepository.findByUserId(doctorId)
                .orElseThrow(() -> new RuntimeException("Bác sĩ không tồn tại."));


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

        newPatient.setUser(creator);

        Patient savedPatient = patientRepository.save(newPatient);

        Appointment newAppointment = new Appointment();
        newAppointment.setPatient(savedPatient);
        newAppointment.setDoctor(selectedDoctor);
        newAppointment.setTime(requestedTime);
        newAppointment.setNote(request.getNote());
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

    @Cacheable(value = "appointments_by_creator",
            key = "#creatorId + '_' + #page + '_' + #sortBy + '_' + #sortDir",
            unless = "#result == null")
    public AppointmentListResponseDTO getAppointmentsByCreatorId(Long creatorId, int page, int size, String sortBy, String sortDir) {
        System.err.println("#######################################################");
        System.err.println("!!! [DEBUG] CACHE MISS - RUNNING DB QUERY FOR CREATOR ID: " + creatorId);
        System.err.println("#######################################################");
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Appointment> appointmentsPage = appointmentRepository.findByCreatorId(creatorId, pageable);

        List<AppointmentDetailDTO> content = appointmentsPage.getContent().stream()
                .map(appointment -> convertToDetailDTO(appointment))
                .toList();

        AppointmentListResponseDTO response = new AppointmentListResponseDTO();
        response.setContent(content);
        response.setPageNo(appointmentsPage.getNumber());
        response.setPageSize(appointmentsPage.getSize());
        response.setTotalElements(appointmentsPage.getTotalElements());
        response.setTotalPages(appointmentsPage.getTotalPages());
        response.setLast(appointmentsPage.isLast());
        System.out.println("-----> [DB HIT] Đang tính toán appointment cho user: " + creatorId);

        return response;
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

        if (appointment.getDiagnosis() != null) {
            appointmentDTO.setHasDiagnosis(true);
            appointmentDTO.setDiagnosisId(appointment.getDiagnosis().getDiagnosisID()); // (Tùy chọn)
        } else {
            appointmentDTO.setHasDiagnosis(false);
            appointmentDTO.setDiagnosisId(null);
        }

        return appointmentDTO;
    }

    @Cacheable(value = "appointments_by_doctor",
            key = "#doctorId + '_' + #date.toString() + '_' + #page",
            unless = "#result == null")
    public AppointmentListResponseDTO getAppointmentsByDoctorIdAndDate(
            Long doctorId,
            LocalDate date,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {
        if (date == null) {
            throw new RuntimeException("Vui lòng cung cấp ngày cụ thể.");
        }

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        Page<Appointment> appointmentsPage = appointmentRepository
                .findByDoctor_UserIdAndTimeBetween(doctorId, startOfDay, endOfDay, pageable);


        List<AppointmentDetailDTO> appointmentDTOs = appointmentsPage.getContent()
                .stream()
                .map(this::convertToDetailDTO)
                .collect(Collectors.toList());

        AppointmentListResponseDTO response = new AppointmentListResponseDTO();
        response.setContent(appointmentDTOs);

        response.setPageNo(appointmentsPage.getNumber());
        response.setPageSize(appointmentsPage.getSize());
        response.setTotalElements(appointmentsPage.getTotalElements());
        response.setTotalPages(appointmentsPage.getTotalPages());
        response.setLast(appointmentsPage.isLast());

        return response;
    }

    @Transactional
    @CacheEvict(value = {"appointments_by_creator", "appointments_by_doctor", "appointment_details", "doctor_slots"}, allEntries = true)
    public void deleteAppointment(Long appointmentId) {
        User currentUser = userService.getCurrentUser();

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn với ID: " + appointmentId));

        AppointmentCancellationLog log = AppointmentCancellationLog.builder()
                .appointmentIdRef(appointment.getAppointmentID())
                .appointmentTime(appointment.getTime())
                .doctorName(appointment.getDoctor() != null ? appointment.getDoctor().getName() : "Unknown")
                .cancelledBy(currentUser)
                .build();

        cancellationLogRepo.save(log);

        try {
            emailService.sendAppointmentCancellationEmail(appointment);
        } catch (Exception e) {
            System.err.println("Sắp xoá lịch hẹn ID: " + appointmentId + " nhưng gửi email huỷ thất bại: " + e.getMessage());
        }
        appointmentRepository.delete(appointment);
    }

    @Transactional
    @CacheEvict(value = {"appointments_by_creator", "appointments_by_doctor", "appointment_details", "doctor_slots"}, allEntries = true)
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

    @Cacheable(value = "appointment_details", key = "#appointmentId")
    public AppointmentInfoForDiagDTO getAppointmentById(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy lịch hẹn với ID: " + appointmentId));

        AppointmentInfoForDiagDTO dto = new AppointmentInfoForDiagDTO();
        dto.setAppointmentID(appointment.getAppointmentID());

        Doctor doctor = appointment.getDoctor();
        DoctorSummaryDTO doctorDTO = new DoctorSummaryDTO();
        doctorDTO.setUserId(doctor.getUserId());
        doctorDTO.setName(doctor.getName());
        doctorDTO.setSpecialty(doctor.getSpecialty());

        dto.setDoctor(doctorDTO);

        Patient patient = appointment.getPatient();
        PatientSummaryDTO patientDTO = new PatientSummaryDTO();
        patientDTO.setId(patient.getPatientId());
        patientDTO.setName(patient.getName());
        patientDTO.setGender(patient.getGender());

        dto.setPatientInfo(patientDTO);

        return dto;
    }

}