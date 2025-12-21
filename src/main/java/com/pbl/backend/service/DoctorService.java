package com.pbl.backend.service;

import com.pbl.backend.dto.request.DoctorCreateRequestDTO;
import com.pbl.backend.dto.request.DoctorEditRequestDTO;
import com.pbl.backend.dto.request.DoctorProfileUpdateRequest;
import com.pbl.backend.dto.response.DoctorDTO;
import com.pbl.backend.dto.response.DoctorEditResponseDTO;
import com.pbl.backend.dto.response.DoctorProfileUpdateResponseDTO;
import com.pbl.backend.dto.response.DoctorSummaryDTO;
import com.pbl.backend.model.Appointment;
import com.pbl.backend.model.Doctor;
import com.pbl.backend.model.Schedule;
import com.pbl.backend.model.User;
import com.pbl.backend.repository.AppointmentRepository;
import com.pbl.backend.repository.DoctorRepository;
import com.pbl.backend.repository.ScheduleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final ScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    @Lazy
    private AppointmentService appointmentService;

    @Cacheable(value = "doctors", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<DoctorDTO> getDoctors(Pageable pageable) {
        Page<Doctor> doctors = doctorRepository.findAll(pageable);
        return doctors.map(DoctorDTO::fromEntity);
    }

    @Cacheable(value = "doctor_summaries", key = "#pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<DoctorSummaryDTO> getDoctorSummaries(Pageable pageable) {
        Page<Doctor> doctors = doctorRepository.findAll(pageable);
        return doctors.map(DoctorSummaryDTO::fromEntity);
    }

    @Cacheable(value = "doctor_details", key = "#id")
    public DoctorDTO getDoctorById(Long id) {
        return doctorRepository.findById(id)
                .map(DoctorDTO::fromEntity)
                .orElse(null);
    }

    @Cacheable(value = "doctors_by_specialty", key = "#specialtyId")
    public List<DoctorSummaryDTO> getDoctorsBySpecialty(Integer specialtyId) {
        String specialtyName = getSpecialtyNameById(specialtyId);

        List<Doctor> doctors = doctorRepository.findActiveBySpecialty(specialtyName);

        return doctors.stream()
                .map(DoctorSummaryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    private Specification<Doctor> buildSearchSpec(String name, String degree, String position) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(name)) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%"
                ));
            }

            if (StringUtils.hasText(degree)) {
                predicates.add(criteriaBuilder.equal(root.get("degree"), degree));
            }

            if (StringUtils.hasText(position)) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("position")),
                        "%" + position.toLowerCase() + "%"
                ));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public Page<DoctorSummaryDTO> searchDoctorSummaries(String name, String degree, String position, Pageable pageable) {
        Specification<Doctor> spec = buildSearchSpec(name, degree, position);

        Page<Doctor> doctors = doctorRepository.findAll(spec, pageable);

        return doctors.map(DoctorSummaryDTO::fromEntity);
    }
    public Page<DoctorDTO> searchDoctors(String name, String degree, String position, Pageable pageable) {
        Specification<Doctor> spec = buildSearchSpec(name, degree, position);
        Page<Doctor> doctors = doctorRepository.findAll(spec, pageable);
        return doctors.map(DoctorDTO::fromEntity);
    }

    @Cacheable(value = "doctor_slots", key = "#doctorId")
    public Map<LocalDate, List<LocalTime>> getAvailableSlotsForDoctor(Long doctorId) {
        List<Schedule> schedules = scheduleRepository.findByDoctorUserIdAndWorkDateAfter(doctorId, LocalDate.now().minusDays(1));
        List<Appointment> appointments = appointmentRepository.findByDoctorUserIdAndTimeAfter(doctorId, LocalDateTime.now().minusHours(1));

        Set<LocalDateTime> bookedSlots = appointments.stream()
                .map(Appointment::getTime)
                .collect(Collectors.toSet());

        Set<LocalDateTime> allPossibleSlots = new HashSet<>();
        for (Schedule schedule : schedules) {
            allPossibleSlots.addAll(generateSlotsForSchedule(schedule));
        }

        List<LocalDateTime> availableSlots = allPossibleSlots.stream()
                .filter(slot -> !bookedSlots.contains(slot) && slot.isAfter(LocalDateTime.now()))
                .collect(Collectors.toList());

        Map<LocalDate, List<LocalTime>> groupedSlots = availableSlots.stream()
                .collect(Collectors.groupingBy(
                        LocalDateTime::toLocalDate,
                        TreeMap::new,
                        Collectors.mapping(LocalDateTime::toLocalTime, Collectors.toList())
                ));

        groupedSlots.forEach((date, times) -> times.sort(LocalTime::compareTo));

        return groupedSlots;
    }

    private String getSpecialtyNameById(Integer specialtyId) {
        if (specialtyId == null) {
            throw new IllegalArgumentException("Chưa chọn chuyên khoa (specialtyId is null).");
        }
        switch (specialtyId) {
            case 1:
                return "Khoa thẩm mỹ";
            case 2:
                return "Khoa khám da";
            default:
                throw new IllegalArgumentException("Chuyên khoa không hợp lệ (Invalid specialtyId: " + specialtyId + ").");
        }
    }

    private List<LocalDateTime> generateSlotsForSchedule(Schedule schedule) {
        List<LocalDateTime> slots = new ArrayList<>();
        LocalDate workDate = schedule.getWorkDate();
        LocalDateTime shiftStartTime = getShiftStartTime(workDate, schedule.getShift());
        LocalDateTime shiftEndTime = getShiftEndTime(workDate, schedule.getShift());
        LocalDateTime currentSlotStart = shiftStartTime;

        while (currentSlotStart.isBefore(shiftEndTime)) {
            slots.add(currentSlotStart);
            currentSlotStart = currentSlotStart.plusMinutes(35);
        }
        return slots;
    }

    private LocalDateTime getShiftStartTime(LocalDate date, Schedule.WorkShift shift) {
        return shift == Schedule.WorkShift.AM ? date.atTime(7, 0) : date.atTime(13, 0);
    }

    private LocalDateTime getShiftEndTime(LocalDate date, Schedule.WorkShift shift) {
        return shift == Schedule.WorkShift.AM ? date.atTime(11, 0) : date.atTime(17, 0);
    }

    @Transactional
    @CacheEvict(value = {"doctors", "doctor_details", "doctor_slots"}, allEntries = true)
    public DoctorDTO createDoctor(DoctorCreateRequestDTO request) {

        if (doctorRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email " + request.getEmail() + " đã được sử dụng bởi bác sĩ khác.");
        }

        Doctor doctor = new Doctor();
        doctor.setName(request.getName());
        doctor.setEmail(request.getEmail());
        doctor.setPosition(request.getPosition());
        doctor.setSpecialty(request.getSpecialty());
        doctor.setPassword(passwordEncoder.encode("12345678"));

        doctor.setRole(User.Role.ROLE_DOCTOR);
        doctor.setAuthStatus("ACTIVE");
        Doctor savedDoctor = doctorRepository.save(doctor);
        return DoctorDTO.fromEntity(savedDoctor);
    }

    public DoctorEditResponseDTO getDoctorForEdit(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + id));

        DoctorEditResponseDTO response = new DoctorEditResponseDTO();
        response.setUserId(doctor.getUserId());
        response.setName(doctor.getName());
        response.setEmail(doctor.getEmail());
        response.setPhoneNumber(doctor.getPhoneNumber());
        response.setPosition(doctor.getPosition());
        response.setDegree(doctor.getDegree());
        response.setSpecialty(doctor.getSpecialty());

        return response;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "doctors", allEntries = true),
            @CacheEvict(value = "doctor_details", key = "#id"),
            @CacheEvict(value = "doctor_slots", key = "#id")
    })
    public DoctorDTO updateDoctor(Long id, DoctorEditRequestDTO request) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + id));

        doctor.setName(request.getName());
        doctor.setEmail(request.getEmail());
        doctor.setPhoneNumber(request.getPhoneNumber());
        doctor.setPosition(request.getPosition());
        doctor.setDegree(request.getDegree());
        doctor.setSpecialty(request.getSpecialty());

        Doctor updatedDoctor = doctorRepository.save(doctor);
        return DoctorDTO.fromEntity(updatedDoctor);
    }

    @Transactional
    @CacheEvict(value = {"doctors", "doctor_details", "doctor_slots", "doctor_summaries", "doctors_by_specialty"}, allEntries = true)
    public void deleteDoctor(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + id));

        if (appointmentService != null) {
            appointmentService.cancelAllFutureAppointmentsForDoctor(id);
        }

        doctor.setAuthStatus("DELETED");

        scheduleRepository.deleteByDoctor_UserIdAndWorkDateAfter(id, LocalDate.now());

        doctorRepository.save(doctor);

        doctorRepository.flush();
    }

    public Doctor getCurrentDoctor() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        System.out.println("DEBUG: Đang tìm doctor với email: [" + email + "]");

        Doctor doctor = doctorRepository.findByEmail(email)
                .orElseThrow(() -> {
                    System.out.println("ERROR: Không tìm thấy Doctor trong DB!");
                    return new RuntimeException("Doctor not found with email: " + email);
                });
        System.out.println("DEBUG: Tìm thấy doctor ID: " + doctor.getUserId());
        return doctor;
    }

    public DoctorProfileUpdateResponseDTO getCurrentDoctorProfile() {
        Doctor doctor = getCurrentDoctor();
        return DoctorProfileUpdateResponseDTO.fromEntity(doctor);
    }

    @Transactional
    @CacheEvict(value = "doctors", allEntries = true)
    public DoctorDTO updateCurrentDoctorProfile(DoctorProfileUpdateRequest request) {
        Doctor doctor = getCurrentDoctor();

        if(request.getName() != null) doctor.setName(request.getName());
        if (request.getPhoneNumber() != null) doctor.setPhoneNumber(request.getPhoneNumber());
        if (request.getYoe() != null) doctor.setYoe(request.getYoe());
        if (request.getIntroduction() != null) doctor.setIntroduction(request.getIntroduction());
        if (request.getDegree() != null) doctor.setDegree(request.getDegree());
        if (request.getAchievements() != null) doctor.setAchievements(request.getAchievements());

        Doctor savedDoctor = doctorRepository.save(doctor);
        return DoctorDTO.fromEntity(savedDoctor);
    }

    @Transactional
    @CacheEvict(value = {"doctors", "doctor_details", "doctor_slots", "doctor_summaries", "doctors_by_specialty"}, allEntries = true)
    public DoctorDTO reopenDoctorAccount(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bác sĩ với id: " + id));

        if (!doctor.getRole().equals(User.Role.ROLE_DOCTOR)) {
            throw new RuntimeException("ID này không phải là tài khoản bác sĩ");
        }

        doctor.setAuthStatus("ACTIVE");

        Doctor savedDoctor = doctorRepository.save(doctor);

        return DoctorDTO.fromEntity(savedDoctor);
    }
}
