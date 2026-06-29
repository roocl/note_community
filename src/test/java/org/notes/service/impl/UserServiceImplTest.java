package org.notes.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.notes.exception.BadRequestException;
import org.notes.exception.NotFoundException;
import org.notes.exception.UnauthorizedException;
import org.notes.mapper.UserMapper;
import org.notes.model.base.AuthResult;
import org.notes.model.base.PageResult;
import org.notes.model.dto.user.LoginRequest;
import org.notes.model.dto.user.RegisterRequest;
import org.notes.model.dto.user.UpdateUserRequest;
import org.notes.model.dto.user.UserQueryParams;
import org.notes.model.entity.User;
import org.notes.model.vo.user.LoginUserVO;
import org.notes.model.vo.user.RegisterVO;
import org.notes.repository.UserSearchRepository;
import org.notes.scope.RequestScopeData;
import org.notes.service.EmailService;
import org.notes.service.EsSyncFailureService;
import org.notes.service.FileService;
import org.notes.service.RedisProtectionService;
import org.notes.utils.JwtUtil;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private FileService fileService;
    @Mock
    private RequestScopeData requestScopeData;
    @Mock
    private EmailService emailService;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private UserSearchRepository userSearchRepository;
    @Mock
    private EsSyncFailureService esSyncFailureService;
    @Mock
    private RedisProtectionService redisProtectionService;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1L);
        user.setAccount("alice");
        user.setPassword("encoded");
        user.setUsername("Alice");
        user.setEmail("alice@example.com");
    }

    @Test
    void register_returnsAuthResult() {
        RegisterRequest request = new RegisterRequest();
        request.setAccount("alice");
        request.setPassword("raw");
        request.setUsername("Alice");
        when(userMapper.findByAccount("alice")).thenReturn(null);
        when(passwordEncoder.encode("raw")).thenReturn("encoded");
        when(jwtUtil.generateToken(null)).thenReturn("token");

        TransactionSynchronizationManager.initSynchronization();
        AuthResult<RegisterVO> result;
        try {
            result = userService.register(request);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }

        assertEquals("token", result.getToken());
        assertNotNull(result.getData());
        verify(userMapper).insert(org.mockito.ArgumentMatchers.any(User.class));
    }

    @Test
    void register_throwsWhenAccountDuplicated() {
        RegisterRequest request = new RegisterRequest();
        request.setAccount("alice");
        when(userMapper.findByAccount("alice")).thenReturn(user);

        assertThrows(BadRequestException.class, () -> userService.register(request));
    }

    @Test
    void login_returnsAuthResultForValidPassword() {
        LoginRequest request = new LoginRequest();
        request.setAccount("alice");
        request.setPassword("raw");
        when(userMapper.findByAccount("alice")).thenReturn(user);
        when(passwordEncoder.matches("raw", "encoded")).thenReturn(true);
        when(jwtUtil.generateToken(1L)).thenReturn("token");

        AuthResult<LoginUserVO> result = userService.login(request);

        assertEquals("token", result.getToken());
        assertEquals("Alice", result.getData().getUsername());
        verify(userMapper).updateLastLoginAt(1L);
    }

    @Test
    void login_throwsWhenPasswordWrong() {
        LoginRequest request = new LoginRequest();
        request.setAccount("alice");
        request.setPassword("raw");
        when(userMapper.findByAccount("alice")).thenReturn(user);
        when(passwordEncoder.matches("raw", "encoded")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> userService.login(request));
    }

    @Test
    void getUserInfo_throwsWhenMissing() {
        when(redisProtectionService.hasNullMarker("user", 9L)).thenReturn(false);
        when(userMapper.findById(9L)).thenReturn(null);

        assertThrows(NotFoundException.class, () -> userService.getUserInfo(9L));
        verify(redisProtectionService).setNullMarker("user", 9L);
    }

    @Test
    void getUserInfo_throwsFromNullMarkerWithoutQueryingMysql() {
        when(redisProtectionService.hasNullMarker("user", 9L)).thenReturn(true);

        assertThrows(NotFoundException.class, () -> userService.getUserInfo(9L));
        verify(userMapper, never()).findById(anyLong());
    }

    @Test
    void getUserList_returnsPagedResult() {
        UserQueryParams params = new UserQueryParams();
        params.setPage(1);
        params.setPageSize(10);
        when(userMapper.countUsersByQueryParam(params)).thenReturn(1);
        when(userMapper.findByQueryParam(params, 10, 0)).thenReturn(List.of(user));

        PageResult<List<User>> result = userService.getUserList(params);

        assertEquals(1, result.getData().size());
        assertEquals(1, result.getPagination().getTotal());
    }

    @Test
    void updateUserInfo_recordsEsSyncFailureWhenSaveFails() {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setUsername("Alice2");
        when(requestScopeData.getUserId()).thenReturn(1L);
        when(userMapper.findById(1L)).thenReturn(user);
        doThrow(new RuntimeException("es down")).when(userSearchRepository).save(any());

        userService.updateUserInfo(request);

        verify(esSyncFailureService).recordFailure(
                eq(EsSyncFailureServiceImpl.ENTITY_USER),
                eq(1L),
                eq(EsSyncFailureServiceImpl.OP_SAVE),
                any(RuntimeException.class));
    }

    @Test
    void getUserMapByIds_returnsEmptyMapForEmptyInput() {
        Map<Long, User> result = userService.getUserMapByIds(List.of());

        assertTrue(result.isEmpty());
        verify(userMapper, never()).findByIdBatch(org.mockito.ArgumentMatchers.anyList());
    }
}
