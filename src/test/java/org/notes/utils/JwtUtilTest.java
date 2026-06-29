package org.notes.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JwtUtil JWT 工具测试")
class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = new JwtUtil();
        // 通过反射设置 @Value 字段
        Field secretField = JwtUtil.class.getDeclaredField("secret");
        secretField.setAccessible(true);
        secretField.set(jwtUtil, "testSecretKeyForJwtUnitTest1234567890");

        Field expirationField = JwtUtil.class.getDeclaredField("expiration");
        expirationField.setAccessible(true);
        expirationField.set(jwtUtil, 3600L); // 1小时
    }

    @Test
    @DisplayName("生成 Token 不为空")
    void generateToken_returnsNonNull() {
        String token = jwtUtil.generateToken(1L);
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("从 Token 中解析出正确的 userId")
    void getUserIdFromToken_returnsCorrectUserId() {
        Long userId = 42L;
        String token = jwtUtil.generateToken(userId);
        Long parsed = jwtUtil.getUserIdFromToken(token);
        assertEquals(userId, parsed);
    }

    @Test
    @DisplayName("无效 Token 解析时返回 null")
    void getUserIdFromToken_invalidToken_returnsNull() {
        Long result = jwtUtil.getUserIdFromToken("invalid.token.here");
        assertNull(result);
    }

    @Test
    @DisplayName("有效 Token 验证通过")
    void validateToken_validToken_returnsTrue() {
        String token = jwtUtil.generateToken(1L);
        assertTrue(jwtUtil.validateToken(token));
    }

    @Test
    @DisplayName("无效 Token 验证不通过")
    void validateToken_invalidToken_returnsFalse() {
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
    }

    @Test
    @DisplayName("refreshToken 返回新 Token")
    void refreshToken_returnsNewToken() {
        Long userId = 10L;
        String token1 = jwtUtil.generateToken(userId);
        String token2 = jwtUtil.refreshToken(userId);
        assertNotNull(token2);
        // 两个 Token 都能解析出同一个 userId
        assertEquals(userId, jwtUtil.getUserIdFromToken(token1));
        assertEquals(userId, jwtUtil.getUserIdFromToken(token2));
    }

    @Test
    @DisplayName("不同 userId 生成不同 Token")
    void generateToken_differentUserIds_differentTokens() {
        String token1 = jwtUtil.generateToken(1L);
        String token2 = jwtUtil.generateToken(2L);
        assertNotEquals(token1, token2);
    }
}
