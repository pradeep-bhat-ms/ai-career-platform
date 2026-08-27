package com.pradeep.aicareerplatform.service;

import com.pradeep.aicareerplatform.entity.PasswordResetOtp;
import com.pradeep.aicareerplatform.repository.PasswordResetOtpRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class OtpService {

    private final PasswordResetOtpRepository otpRepository;
    private final JavaMailSender mailSender;

    private static final int OTP_LENGTH = 6;
    private static final int EXPIRY_MINUTES = 10;

    public OtpService(PasswordResetOtpRepository otpRepository, JavaMailSender mailSender) {
        this.otpRepository = otpRepository;
        this.mailSender = mailSender;
    }

    public void generateAndSendOtp(String email) {
        String otpCode = generateOtpCode();

        PasswordResetOtp otp = new PasswordResetOtp();
        otp.setEmail(email);
        otp.setOtpCode(otpCode);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES));
        otpRepository.save(otp);

        sendOtpEmail(email, otpCode);
    }

    public boolean verifyOtp(String email, String submittedCode) {
        PasswordResetOtp otp = otpRepository
                .findTopByEmailAndUsedFalseOrderByCreatedAtDesc(email)
                .orElse(null);

        if (otp == null) {
            return false;
        }
        if (otp.isUsed() || otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }
        if (!otp.getOtpCode().equals(submittedCode)) {
            return false;
        }

        otp.setUsed(true);
        otpRepository.save(otp);
        return true;
    }

    private String generateOtpCode() {
        SecureRandom random = new SecureRandom();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    private void sendOtpEmail(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Your Password Reset Code - AI Career Platform");
        message.setText("Your OTP code is: " + otpCode + "\n\nThis code expires in " + EXPIRY_MINUTES + " minutes.\n\nIf you did not request this, please ignore this email.");
        mailSender.send(message);
    }
}