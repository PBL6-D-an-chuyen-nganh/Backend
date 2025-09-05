package com.pbl.backend.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OTPService {
    private final Random random = new Random();

    // Lưu OTP: email -> {otp + expiredAt}
    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();

    private static final int EXPIRE_MINUTES = 5; // OTP hết hạn sau 5 phút

    public String generateOtp(String email) {
        int otp = 100000 + random.nextInt(900000);
        String otpStr = String.valueOf(otp);

        // Lưu OTP + thời gian hết hạn
        otpStorage.put(email, new OtpData(otpStr, LocalDateTime.now().plusMinutes(EXPIRE_MINUTES)));

        return otpStr;
    }

    public boolean verifyOtp(String email, String otp) {
        if (!otpStorage.containsKey(email)) {
            return false;
        }

        OtpData data = otpStorage.get(email);

        // Check expired
        if (LocalDateTime.now().isAfter(data.getExpiredAt())) {
            otpStorage.remove(email); // Xóa khi hết hạn
            return false;
        }

        // Check match
        boolean isValid = data.getOtp().equals(otp);

        if (isValid) {
            otpStorage.remove(email); // Xóa luôn sau khi dùng
        }

        return isValid;
    }

    // Class nội bộ để lưu OTP + expiredAt
    private static class OtpData {
        private final String otp;
        private final LocalDateTime expiredAt;

        public OtpData(String otp, LocalDateTime expiredAt) {
            this.otp = otp;
            this.expiredAt = expiredAt;
        }

        public String getOtp() {
            return otp;
        }

        public LocalDateTime getExpiredAt() {
            return expiredAt;
        }
    }
}
