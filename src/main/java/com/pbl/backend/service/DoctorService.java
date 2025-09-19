package com.pbl.backend.service;

import com.pbl.backend.dto.DoctorDTO;
import com.pbl.backend.dto.UserDTO;
import com.pbl.backend.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorService {
    @Autowired
    private DoctorRepository doctorRepo;

    public List<DoctorDTO> getAllDoctors() {
        return doctorRepo.findAll().stream()
                .map(DoctorDTO::fromEntity)
                .collect(Collectors.toList());
    }

}
