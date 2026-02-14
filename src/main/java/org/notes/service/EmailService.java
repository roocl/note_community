package org.notes.service;

public interface EmailService {
    void sendVerificationCode(String email);

    boolean checkVerificationCode(String email, String code);

    boolean isVerificationCodeRateLimited(String email);
}
