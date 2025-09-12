package com.pbl.backend.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String to, String otp) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject("Your OTP Code");

            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px; background-color: #f4f4f4;'>"
                    + "<div style='max-width: 600px; margin: auto; background: #fff; padding: 20px; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);'>"
                    + "<h2 style='color: #333;'>Your OTP Code</h2>"
                    + "<p style='font-size: 16px; color: #555;'>Xin chào,</p>"
                    + "<p style='font-size: 16px; color: #555;'>Mã OTP của bạn là:</p>"
                    + "<p style='font-size: 24px; font-weight: bold; color: #007bff; text-align: center; letter-spacing: 3px;'>" + otp + "</p>"
                    + "<p style='font-size: 14px; color: #777;'>Mã này sẽ hết hạn sau 2 phút. Vui lòng không chia sẻ cho bất kỳ ai.</p>"
                    + "<hr style='margin: 20px 0;'>"
                    + "<p style='font-size: 12px; color: #aaa;'>Đây là email tự động, vui lòng không trả lời.</p>"
                    + "</div></div>";

            helper.setText(htmlContent, true); // true = HTML

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }
}
