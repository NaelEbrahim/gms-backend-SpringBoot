package com.graduation.GMS.Services.GeneralServices;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class VerificationCodeService {

    private final JavaMailSender mailSender;

    private final CacheManager cacheManager;


    public void sendVerificationCode(String email, String userFirstName, String userLastName) {
        String code = generateRandomCode();
        CodeEntry entry = new CodeEntry(code, LocalDateTime.now());
        Cache cache = cacheManager.getCache("passwordResetCodes");
        if (cache != null) {
            cache.put(email, entry);
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset Verification Code In ShapeUp Account");
        message.setText("Hey " + userFirstName + " " + userLastName + " !" + "\nYour verification code is: " + code +
                "\nDon't share it with anyone" + "\nThis code will expire in 10 minutes.\ntimeStamp: " + LocalDateTime.now());
        mailSender.send(message);
    }

    public boolean verifyCode(String email, String code) {
        Cache cache = cacheManager.getCache("passwordResetCodes");
        CodeEntry entry = cache != null ? cache.get(email, CodeEntry.class) : null;
        if (entry == null) return false;
        boolean isCodeValid = code != null && code.equals(entry.code());
        boolean isNotExpired = entry.generatedAt().plusMinutes(10).isAfter(LocalDateTime.now());
        if (!isNotExpired)
            clearCode(email);
        return isCodeValid && isNotExpired;
    }

    public void clearCode(String email) {
        Cache cache = cacheManager.getCache("passwordResetCodes");
        if (cache != null)
            cache.evict(email);
    }

    private String generateRandomCode() {
        SecureRandom random = new SecureRandom();
        return String.valueOf(100000 + random.nextInt(900000));
    }

    private record CodeEntry(String code, LocalDateTime generatedAt) {
    }

}
