package com.pbl.backend.service;

import com.pbl.backend.dto.DoctorDTO;
import com.pbl.backend.model.Doctor;
import com.pbl.backend.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepo;

    public Page<DoctorDTO> getDoctors(Pageable pageable) {
        Page<Doctor> doctors = doctorRepo.findAll(pageable);
        return doctors.map(DoctorDTO::fromEntity);
    }
}
