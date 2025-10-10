package com.pbl.backend.service;

import com.pbl.backend.dto.DoctorDTO;
import com.pbl.backend.model.Doctor;
import com.pbl.backend.repository.DoctorRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepo;

    public Page<DoctorDTO> getDoctors(Pageable pageable) {
        Page<Doctor> doctors = doctorRepo.findAll(pageable);
        return doctors.map(DoctorDTO::fromEntity);
    }

    public DoctorDTO getDoctorById(Integer id) {
        return doctorRepo.findById(id)
                .map(DoctorDTO::fromEntity)
                .orElse(null);
    }

    public Page<DoctorDTO> searchDoctors(String name, String degree, String position, Pageable pageable) {
        Specification<Doctor> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(name)) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%"
                ));
            }

            // 2. Lọc theo bằng cấp
            if (StringUtils.hasText(degree)) {
                predicates.add(criteriaBuilder.equal(root.get("degree"), degree));
            }

            // 3. Lọc theo chức vụ
            if (StringUtils.hasText(position)) {
                predicates.add(criteriaBuilder.equal(root.get("position"), position));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<Doctor> doctors = doctorRepo.findAll(spec, pageable);
        return doctors.map(DoctorDTO::fromEntity);
    }


}
