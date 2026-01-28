package org.notes.model.enums;

public class RedisKey {
    public static String registerVerificationCode(String email) {
        return "email:register_verification_code:" + email;
    }

    public static String registerVerificationLimitCode(String email) {
        return "email:register_verification_code:limit:" + email;
    }

    public static String emailTaskQueue() {
        return "queue:email:task";
    }
}
