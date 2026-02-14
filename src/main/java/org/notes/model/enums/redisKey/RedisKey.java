package org.notes.model.enums.redisKey;

public class RedisKey {
    public static String registerVerificationCode(String email) {
        return "email:register_verification_code:" + email;
    }

    public static String registerVerificationLimitCode(String email) {
        return "email:register_verification_code:limit:" + email;
    }
}
