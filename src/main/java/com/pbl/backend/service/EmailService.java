package com.pbl.backend.service;

import com.pbl.backend.model.Appointment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    // Lấy cấu hình từ application.properties
    @Value("${brevo.api.key}")
    private String apiKey;

    @Value("${brevo.api.url}")
    private String apiUrl;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name}")
    private String senderName;

    private final RestTemplate restTemplate = new RestTemplate();

    // Hàm chung để gọi API Brevo
    private void sendEmailViaApi(String toEmail, String subject, String htmlContent) {
        try {
            // 1. Tạo Header
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey); // Header xác thực của Brevo

            // 2. Tạo Body JSON
            Map<String, Object> body = new HashMap<>();

            // Thông tin người gửi
            Map<String, String> sender = new HashMap<>();
            sender.put("name", senderName);
            sender.put("email", senderEmail);
            body.put("sender", sender);

            // Thông tin người nhận (List)
            Map<String, String> to = new HashMap<>();
            to.put("email", toEmail);
            body.put("to", Collections.singletonList(to));

            // Nội dung
            body.put("subject", subject);
            body.put("htmlContent", htmlContent);

            // 3. Đóng gói Request
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            // 4. Gửi POST Request
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Gửi mail API thành công tới: {}", toEmail);
            } else {
                logger.error("Lỗi gửi mail API: Status Code {}", response.getStatusCode());
            }

        } catch (Exception e) {
            logger.error("EXCEPTION Gửi mail API tới {}: {}", toEmail, e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void sendOtpEmail(String to, String otp) {
        logger.info("Đang gửi OTP qua API tới: {}", to);

        String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px; background-color: #f4f4f4;'>"
                + "<div style='max-width: 600px; margin: auto; background: #fff; padding: 20px; border-radius: 10px; box-shadow: 0 4px 6px rgba(0,0,0,0.1);'>"
                + "<h2 style='color: #333;'>Your OTP Code</h2>"
                + "<p style='font-size: 16px; color: #555;'>Xin chào,</p>"
                + "<p style='font-size: 16px; color: #555;'>Mã OTP của bạn là:</p>"
                + "<p style='font-size: 24px; font-weight: bold; color: #007bff; text-align: center; letter-spacing: 3px;'>" + otp + "</p>"
                + "<p style='font-size: 14px; color: #777;'>Mã này sẽ hết hạn sau 2 phút.</p>"
                + "</div></div>";

        sendEmailViaApi(to, "Mã xác thực OTP - SkinPlus", htmlContent);
    }

    @Async
    public void sendAppointmentConfirmationEmail(Appointment appointment) {
        String patientEmail = appointment.getPatient().getEmail();
        if (patientEmail == null || patientEmail.trim().isEmpty()) return;

        logger.info("Đang gửi xác nhận lịch hẹn ID: {}", appointment.getAppointmentID());

        String patientName = appointment.getPatient().getName();
        String doctorName = appointment.getDoctor().getName();
        String doctorPosition = appointment.getDoctor().getPosition();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm 'ngày' dd/MM/yyyy");
        String appointmentTime = appointment.getTime().format(formatter);

        String htmlContent = "<div style='font-family: Arial, sans-serif; line-height: 1.6; padding: 20px; background-color: #f9f9f9;'>"
                + "<div style='max-width: 600px; margin: auto; background: #ffffff; padding: 30px; border-radius: 12px;'>"
                + "<h1 style='color: #0056b3; text-align: center;'>Xác nhận Lịch hẹn</h1>"
                + "<p>Xin chào <strong>" + patientName + "</strong>,</p>"
                + "<p>Lịch hẹn của bạn với bác sĩ <strong>" + doctorName + "</strong> (" + (doctorPosition!=null?doctorPosition:"") + ") vào lúc <strong>" + appointmentTime + "</strong> đã được xác nhận.</p>"
                + "<p>Ghi chú: " + (appointment.getNote() != null ? appointment.getNote() : "Không có") + "</p>"
                + "<hr><p style='font-size:12px; color:#aaa'>Email tự động từ SkinPlus.</p>"
                + "</div></div>";

        sendEmailViaApi(patientEmail, "Xác nhận Lịch hẹn Thành công", htmlContent);
    }

    @Async
    public void sendAppointmentCancellationEmail(Appointment appointment) {
        String patientEmail = appointment.getPatient().getEmail();
        if (patientEmail == null || patientEmail.trim().isEmpty()) return;

        logger.info("Đang gửi huỷ lịch hẹn ID: {}", appointment.getAppointmentID());

        String patientName = appointment.getPatient().getName();
        String doctorName = appointment.getDoctor().getName();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm 'ngày' dd/MM/yyyy");
        String appointmentTime = appointment.getTime().format(formatter);

        String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px;'>"
                + "<h1 style='color: #d9534f;'>Đã Huỷ Lịch hẹn</h1>"
                + "<p>Xin chào <strong>" + patientName + "</strong>,</p>"
                + "<p>Lịch hẹn với bác sĩ <strong>" + doctorName + "</strong> vào lúc <strong>" + appointmentTime + "</strong> đã được huỷ thành công.</p>"
                + "</div>";

        sendEmailViaApi(patientEmail, "Thông báo Huỷ Lịch hẹn", htmlContent);
    }

    @Async
    public void sendAppointmentDoctorCancellationEmail(String patientEmail, String patientName, String doctorName, LocalDateTime time) {

        if (patientEmail == null || patientEmail.trim().isEmpty()) return;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm 'ngày' dd/MM/yyyy");
        String appointmentTimeStr = time.format(formatter);

        String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px;'>"
                + "<h1 style='color: #d9534f;'>Bác sĩ Huỷ Lịch hẹn</h1>"
                + "<p>Xin chào <strong>" + patientName + "</strong>,</p>"
                + "<p>Rất tiếc, bác sĩ <strong>" + doctorName + "</strong> đã huỷ lịch hẹn lúc <strong>" + appointmentTimeStr + "</strong>.</p>"
                + "<p>Vui lòng đặt lại lịch mới.</p>"
                + "</div>";

        sendEmailViaApi(patientEmail, "Thông báo: Bác sĩ đã huỷ lịch hẹn", htmlContent);
    }
}