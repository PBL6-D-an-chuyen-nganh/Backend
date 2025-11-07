package com.pbl.backend.service;

import com.pbl.backend.model.Appointment;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

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

    public void sendAppointmentConfirmationEmail(Appointment appointment) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String patientEmail = appointment.getPatient().getEmail();
            String patientName = appointment.getPatient().getName();
            String doctorName = appointment.getDoctor().getName();
            String doctorPosition = appointment.getDoctor().getPosition();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm 'ngày' dd/MM/yyyy");
            String appointmentTime = appointment.getTime().format(formatter);

            helper.setTo(patientEmail);
            helper.setSubject("Xác nhận Lịch hẹn Khám bệnh Thành công (ID: " + appointment.getAppointmentID() + ")");

            String htmlContent = "<div style='font-family: Arial, sans-serif; line-height: 1.6; padding: 20px; background-color: #f9f9f9;'>"
                    + "<div style='max-width: 600px; margin: auto; background: #ffffff; padding: 30px; border-radius: 12px; box-shadow: 0 6px 15px rgba(0,0,0,0.08);'>"
                    + "<h1 style='color: #0056b3; text-align: center; border-bottom: 2px solid #eee; padding-bottom: 10px;'>Xác nhận Lịch hẹn</h1>"
                    + "<p style='font-size: 16px; color: #333;'>Xin chào, <strong>" + patientName + "</strong>,</p>"
                    + "<p style='font-size: 16px; color: #333;'>Lịch hẹn của bạn đã được xác nhận thành công. Dưới đây là thông tin chi tiết:</p>"

                    + "<div style='background-color: #f4f7fa; padding: 20px; border-radius: 8px; margin: 20px 0;'>"
                    + "<table style='width: 100%; border-collapse: collapse;'>"
                    + "<tr>"
                    + "<td style='padding: 10px; font-size: 16px; color: #555; width: 120px;'><strong>Bác sĩ:</strong></td>"
                    + "<td style='padding: 10px; font-size: 16px; color: #000;'>" + doctorName + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td style='padding: 10px; font-size: 16px; color: #555;'><strong>Chức vụ:</strong></td>"
                    + "<td style='padding: 10px; font-size: 16px; color: #000;'>" + (doctorPosition != null ? doctorPosition : "N/A") + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td style='padding: 10px; font-size: 16px; color: #555;'><strong>Thời gian:</strong></td>"
                    + "<td style='padding: 10px; font-size: 16px; color: #000; font-weight: bold;'>" + appointmentTime + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td style='padding: 10px; font-size: 16px; color: #555; vertical-align: top;'><strong>Ghi chú:</strong></td>"
                    + "<td style='padding: 10px; font-size: 16px; color: #000;'>" + (appointment.getNote() != null ? appointment.getNote() : "Không có") + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</div>"
                    + "<p style='font-size: 16px; color: #333; margin-top: 25px;'>Cảm ơn bạn đã tin tưởng và sử dụng dịch vụ của chúng tôi.</p>"
                    + "<hr style='margin: 30px 0 15px 0; border: none; border-top: 1px solid #eee;'>"
                    + "<p style='font-size: 12px; color: #aaa; text-align: center;'>Đây là email tự động, vui lòng không trả lời.</p>"
                    + "</div></div>";

            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email xác nhận: " + e.getMessage(), e);
        }
    }
    public void sendAppointmentCancellationEmail(Appointment appointment) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String patientEmail = appointment.getPatient().getEmail();
            String patientName = appointment.getPatient().getName();
            String doctorName = appointment.getDoctor().getName();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm 'ngày' dd/MM/yyyy");
            String appointmentTime = appointment.getTime().format(formatter);

            helper.setTo(patientEmail);
            helper.setSubject("Thông báo Huỷ Lịch hẹn (ID: " + appointment.getAppointmentID() + ")");

            String htmlContent = "<div style='font-family: Arial, sans-serif; line-height: 1.6; padding: 20px; background-color: #f9f9f9;'>"
                    + "<div style='max-width: 600px; margin: auto; background: #ffffff; padding: 30px; border-radius: 12px; box-shadow: 0 6px 15px rgba(0,0,0,0.08);'>"
                    + "<h1 style='color: #d9534f; text-align: center; border-bottom: 2px solid #eee; padding-bottom: 10px;'>Đã Huỷ Lịch hẹn</h1>"
                    + "<p style='font-size: 16px; color: #333;'>Xin chào, <strong>" + patientName + "</strong>,</p>"
                    + "<p style='font-size: 16px; color: #333;'>Chúng tôi xác nhận lịch hẹn của bạn đã được huỷ thành công. Dưới đây là thông tin lịch hẹn đã huỷ:</p>"

                    + "<div style='background-color: #fef7f7; padding: 20px; border-radius: 8px; margin: 20px 0; border: 1px solid #f0c7c7;'>"
                    + "<table style='width: 100%; border-collapse: collapse;'>"
                    + "<tr>"
                    + "<td style='padding: 10px; font-size: 16px; color: #555; width: 120px;'><strong>Bác sĩ:</strong></td>"
                    + "<td style='padding: 10px; font-size: 16px; color: #000;'>" + doctorName + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td style='padding: 10px; font-size: 16px; color: #555;'><strong>Thời gian:</strong></td>"
                    + "<td style='padding: 10px; font-size: 16px; color: #000; font-weight: bold;'>" + appointmentTime + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</div>"

                    + "<p style='font-size: 16px; color: #333; margin-top: 25px;'>Nếu việc huỷ này là một sai sót, vui lòng liên hệ chúng tôi hoặc đặt lại lịch hẹn mới.</p>"
                    + "<p style='font-size: 16px; color: #333;'>Cảm ơn bạn đã sử dụng dịch vụ.</p>"

                    + "<hr style='margin: 30px 0 15px 0; border: none; border-top: 1px solid #eee;'>"
                    + "<p style='font-size: 12px; color: #aaa; text-align: center;'>Đây là email tự động, vui lòng không trả lời.</p>"
                    + "</div></div>";

            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email huỷ lịch: " + e.getMessage(), e);
        }
    }
    public void sendAppointmentDoctorCancellationEmail(Appointment appointment) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String patientEmail = appointment.getPatient().getEmail();
            String patientName = appointment.getPatient().getName();
            String doctorName = appointment.getDoctor().getName();
            String note = appointment.getNote() != null ? appointment.getNote() : "Không có ghi chú.";

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm 'ngày' dd/MM/yyyy");
            String appointmentTime = appointment.getTime().format(formatter);

            helper.setTo(patientEmail);
            helper.setSubject("Thông báo: Bác sĩ đã huỷ lịch hẹn (ID: " + appointment.getAppointmentID() + ")");

            String htmlContent = "<div style='font-family: Arial, sans-serif; line-height: 1.6; padding: 20px; background-color: #f9f9f9;'>"
                    + "<div style='max-width: 600px; margin: auto; background: #ffffff; padding: 30px; border-radius: 12px; box-shadow: 0 6px 15px rgba(0,0,0,0.08);'>"
                    + "<h1 style='color: #d9534f; text-align: center; border-bottom: 2px solid #eee; padding-bottom: 10px;'>Thông báo Huỷ Lịch hẹn</h1>"

                    + "<p style='font-size: 16px; color: #333;'>Xin chào, <strong>" + patientName + "</strong>,</p>"
                    + "<p style='font-size: 16px; color: #333;'>Rất tiếc, bác sĩ <strong>" + doctorName + "</strong> "
                    + "đã huỷ lịch hẹn của bạn vì lý do cá nhân hoặc thay đổi lịch làm việc.</p>"

                    + "<div style='background-color: #fef7f7; padding: 20px; border-radius: 8px; margin: 20px 0; border: 1px solid #f0c7c7;'>"
                    + "<table style='width: 100%; border-collapse: collapse;'>"
                    + "<tr>"
                    + "<td style='padding: 10px; font-size: 16px; color: #555; width: 140px;'><strong>Bác sĩ:</strong></td>"
                    + "<td style='padding: 10px; font-size: 16px; color: #000;'>" + doctorName + "</td>"
                    + "</tr>"
                    + "<tr>"
                    + "<td style='padding: 10px; font-size: 16px; color: #555;'><strong>Thời gian hẹn:</strong></td>"
                    + "<td style='padding: 10px; font-size: 16px; color: #000; font-weight: bold;'>" + appointmentTime + "</td>"
                    + "</tr>"
                    + "</table>"
                    + "</div>"

                    + "<p style='font-size: 16px; color: #333;'>Chúng tôi rất xin lỗi về sự bất tiện này. "
                    + "Bạn có thể <strong>đặt lại lịch hẹn mới</strong> thông qua hệ thống hoặc chọn bác sĩ khác phù hợp.</p>"

                    + "<p style='font-size: 16px; color: #333;'>Cảm ơn bạn đã thông cảm và sử dụng dịch vụ của chúng tôi.</p>"

                    + "<hr style='margin: 30px 0 15px 0; border: none; border-top: 1px solid #eee;'>"
                    + "<p style='font-size: 12px; color: #aaa; text-align: center;'>Đây là email tự động, vui lòng không trả lời.</p>"
                    + "</div></div>";

            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email huỷ lịch do bác sĩ: " + e.getMessage(), e);
        }
    }

}
