package com.pbl.backend.service;

import com.pbl.backend.model.Verification;
import com.pbl.backend.repository.VerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class OTPService {
    @Autowired
    private VerificationRepository verificationRepository;
    private static final int EXPIRATION_MINUTES = 2;

    public String generateOTP(String email) {
        int otp = 100000 + new Random().nextInt(900000);
        String otpStr = String.valueOf(otp);

        Verification verification = new Verification();
        verification.setEmail(email);
        verification.setOtp(otpStr);
        verification.setExpiredAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES));
        verificationRepository.save(verification);
        return otpStr;
    }

    public boolean verifyOtp(String email, String otp) {
        return verificationRepository.findTopByEmailOrderByExpiredAtDesc(email)
                .map(v -> {
                    if (LocalDateTime.now().isAfter(v.getExpiredAt())) {
                        verificationRepository.delete(v);
                        return false;
                    }
                    if (v.getOtp().equals(otp)) {
                        verificationRepository.delete(v);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    public void clearOtp(String email) {
        verificationRepository.findByEmail(email).ifPresent(verificationRepository::delete);
    }
}
