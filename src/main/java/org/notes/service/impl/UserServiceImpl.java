package org.notes.service.impl;

import cn.hutool.core.util.StrUtil;
import lombok.extern.log4j.Log4j2;
import org.notes.annotation.NeedLogin;
import org.notes.mapper.UserMapper;
import org.notes.model.base.ApiResponse;
import org.notes.model.base.Pagination;
import org.notes.model.dto.user.LoginRequest;
import org.notes.model.dto.user.RegisterRequest;
import org.notes.model.dto.user.UpdateUserRequest;
import org.notes.model.dto.user.UserQueryParams;
import org.notes.model.entity.User;
import org.notes.model.vo.user.AvatarVO;
import org.notes.model.vo.user.LoginUserVO;
import org.notes.model.vo.user.RegisterVO;
import org.notes.model.vo.user.UserVO;
import org.notes.model.es.UserDocument;
import org.notes.scope.RequestScopeData;
import org.notes.repository.UserSearchRepository;
import org.notes.service.EmailService;
import org.notes.service.FileService;
import org.notes.service.UserService;
import org.notes.utils.ApiResponseUtil;
import org.notes.utils.JwtUtil;
import org.notes.utils.PaginationUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Log4j2
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private FileService fileService;

    @Autowired
    private RequestScopeData requestScopeData;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserSearchRepository userSearchRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<RegisterVO> register(RegisterRequest request) {
        // 检查账号是否已存在
        User existingUser = userMapper.findByAccount(request.getAccount());
        if (existingUser != null) {
            return ApiResponseUtil.error("账号重复");
        }
        // 如果提供了邮箱，则进行邮箱相关验证
        if (StrUtil.isNotBlank(request.getEmail())) {
            existingUser = userMapper.findByEmail(request.getEmail());
            // 检查邮箱是否已存在
            if (existingUser != null) {
                return ApiResponseUtil.error("邮箱已被使用");
            }
            // 如果提供了邮箱但没有提供验证码
            if (StrUtil.isBlank(request.getVerifyCode())) {
                return ApiResponseUtil.error("请提供邮箱验证码");
            }
            // 验证邮箱验证码
            if (!emailService.checkVerificationCode(request.getEmail(), request.getVerifyCode())) {
                return ApiResponseUtil.error("验证码无效或已过期");
            }
        }

        // 创建新用户
        User user = new User();
        BeanUtils.copyProperties(request, user);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        try {
            // 保存用户
            userMapper.insert(user);

            // 生成JWT
            String token = jwtUtil.generateToken(user.getUserId());

            // 映射用户信息到VO并更新登录时间
            RegisterVO registerVO = new RegisterVO();
            BeanUtils.copyProperties(user, registerVO);
            userMapper.updateLastLoginAt(user.getUserId());

            return ApiResponseUtil.success("注册成功", registerVO, token);
        } catch (Exception e) {
            log.error("注册失败", e);
            return ApiResponseUtil.error("注册失败，请稍后再试");
        } finally {
            // 同步用户到 Elasticsearch
            syncUserToEs(user);
        }
    }

    @Override
    public ApiResponse<LoginUserVO> login(LoginRequest request) {
        User user = null;

        // 根据账号或邮箱查找用户
        if (StrUtil.isNotBlank(request.getAccount())) {
            user = userMapper.findByAccount(request.getAccount());
        } else if (StrUtil.isNotBlank(request.getEmail())) {
            user = userMapper.findByEmail(request.getEmail());
        } else {
            return ApiResponseUtil.error("请提供账号或邮箱");
        }

        // 验证账号以及密码
        if (user == null) {
            return ApiResponseUtil.error("用户不存在");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ApiResponseUtil.error("密码错误");
        }

        // 生成JWT
        String token = jwtUtil.generateToken(user.getUserId());

        // 映射用户信息到VO并更新登录时间
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        userMapper.updateLastLoginAt(user.getUserId());

        return ApiResponseUtil.success("登录成功", loginUserVO, token);
    }

    @Override
    public ApiResponse<LoginUserVO> whoami() {
        Long userId = requestScopeData.getUserId();

        // 验证用户ID状态
        if (userId == null) {
            return ApiResponseUtil.error("用户ID异常");
        }

        try {
            // 查询用户信息
            User user = userMapper.findById(userId);
            if (user == null) {
                return ApiResponseUtil.error("用户不存在");
            }

            // 生成新的JWT
            String newToken = jwtUtil.generateToken(user.getUserId());

            // 映射用户信息到VO并更新登录时间
            LoginUserVO loginUserVO = new LoginUserVO();
            BeanUtils.copyProperties(user, loginUserVO);
            userMapper.updateLastLoginAt(user.getUserId());

            // 返回响应
            return ApiResponseUtil.success("自动登录成功", loginUserVO, newToken);
        } catch (Exception e) {
            return ApiResponseUtil.error("系统错误");
        }

    }

    @Override
    public ApiResponse<UserVO> getUserInfo(Long userId) {
        User user = userMapper.findById(userId);

        // 验证用户是否存在
        if (user == null) {
            return ApiResponseUtil.error("用户不存在");
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        return ApiResponseUtil.success("获取用户信息成功", userVO);
    }

    @Override
    @Transactional
    @NeedLogin
    public ApiResponse<LoginUserVO> updateUserInfo(UpdateUserRequest request) {
        Long userId = requestScopeData.getUserId();

        // 验证用户ID状态
        if (userId == null) {
            return ApiResponseUtil.error("用户ID异常");
        }

        User user = new User();
        BeanUtils.copyProperties(request, user);
        user.setUserId(userId);

        System.out.println(user);

        try {
            userMapper.update(user);

            // 同步到 Elasticsearch
            User fullUser = userMapper.findById(userId);
            syncUserToEs(fullUser);

            return ApiResponseUtil.success("更新用户信息成功");
        } catch (Exception e) {
            return ApiResponseUtil.error("更新用户信息失败");
        }
    }

    @Override
    public ApiResponse<AvatarVO> uploadAvatar(MultipartFile file) {
        try {
            String url = fileService.uploadImage(file);
            AvatarVO avatarVO = new AvatarVO();
            BeanUtils.copyProperties(file, avatarVO);
            return ApiResponseUtil.success("上传头像成功", avatarVO);
        } catch (Exception e) {
            return ApiResponseUtil.error(e.getMessage());
        }
    }

    @Override
    public ApiResponse<List<User>> getUserList(UserQueryParams queryParam) {
        // 分页数据
        int total = userMapper.countUsersByQueryParam(queryParam);
        int offset = PaginationUtils.calculateOffset(queryParam.getPage(), queryParam.getPageSize());
        Pagination pagination = new Pagination(queryParam.getPage(), queryParam.getPageSize(), total);

        try {
            List<User> users = userMapper.findByQueryParam(queryParam, queryParam.getPageSize(), offset);
            return ApiResponseUtil.success("获取用户列表成功", users, pagination);
        } catch (Exception e) {
            return ApiResponseUtil.error(e.getMessage());
        }
    }

    @Override
    public Map<Long, User> getUserMapByIds(List<Long> authorIds) {
        if (authorIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<User> users = userMapper.findByIdBatch(authorIds);

        return users.stream().collect(Collectors.toMap(User::getUserId, user -> user));
    }

    /**
     * 同步用户到 Elasticsearch
     */
    private void syncUserToEs(User user) {
        try {
            if (user == null || user.getUserId() == null)
                return;
            UserDocument doc = new UserDocument();
            BeanUtils.copyProperties(user, doc);
            userSearchRepository.save(doc);
        } catch (Exception e) {
            log.warn("同步用户到ES失败，userId={}", user != null ? user.getUserId() : null, e);
        }
    }
}
