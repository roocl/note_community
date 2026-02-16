package org.notes.service.impl;

import cn.hutool.core.util.StrUtil;
import lombok.extern.log4j.Log4j2;
import org.notes.annotation.NeedLogin;
import org.notes.exception.BadRequestException;
import org.notes.exception.BaseException;
import org.notes.exception.NotFoundException;
import org.notes.exception.UnauthorizedException;
import org.notes.mapper.UserMapper;
import org.notes.model.base.AuthResult;
import org.notes.model.base.PageResult;
import org.notes.model.base.Pagination;
import org.notes.model.dto.user.LoginRequest;
import org.notes.model.dto.user.RegisterRequest;
import org.notes.model.dto.user.UpdateUserRequest;
import org.notes.model.dto.user.UserQueryParams;
import org.notes.model.entity.User;
import org.notes.model.es.UserDocument;
import org.notes.model.vo.user.AvatarVO;
import org.notes.model.vo.user.LoginUserVO;
import org.notes.model.vo.user.RegisterVO;
import org.notes.model.vo.user.UserVO;
import org.notes.repository.UserSearchRepository;
import org.notes.scope.RequestScopeData;
import org.notes.service.EmailService;
import org.notes.service.FileService;
import org.notes.service.UserService;
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
    public AuthResult<RegisterVO> register(RegisterRequest request) {
        User existingUser = userMapper.findByAccount(request.getAccount());
        if (existingUser != null) {
            throw new BadRequestException("账号重复");
        }

        if (StrUtil.isNotBlank(request.getEmail())) {
            existingUser = userMapper.findByEmail(request.getEmail());
            if (existingUser != null) {
                throw new BadRequestException("邮箱已被使用");
            }
            if (StrUtil.isBlank(request.getVerifyCode())) {
                throw new BadRequestException("请提供邮箱验证码");
            }
            if (!emailService.checkVerificationCode(request.getEmail(), request.getVerifyCode())) {
                throw new BadRequestException("验证码无效或已过期");
            }
        }

        User user = new User();
        BeanUtils.copyProperties(request, user);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        try {
            userMapper.insert(user);
            String token = jwtUtil.generateToken(user.getUserId());

            RegisterVO registerVO = new RegisterVO();
            BeanUtils.copyProperties(user, registerVO);
            userMapper.updateLastLoginAt(user.getUserId());

            return new AuthResult<>(registerVO, token);
        } catch (Exception e) {
            log.error("注册失败", e);
            throw new BaseException("注册失败，请稍后再试");
        } finally {
            syncUserToEs(user);
        }
    }

    @Override
    public AuthResult<LoginUserVO> login(LoginRequest request) {
        User user;

        if (StrUtil.isNotBlank(request.getAccount())) {
            user = userMapper.findByAccount(request.getAccount());
        } else if (StrUtil.isNotBlank(request.getEmail())) {
            user = userMapper.findByEmail(request.getEmail());
        } else {
            throw new BadRequestException("请提供账号或邮箱");
        }

        if (user == null) {
            throw new NotFoundException("用户不存在");
        }
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("密码错误");
        }

        String token = jwtUtil.generateToken(user.getUserId());

        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        userMapper.updateLastLoginAt(user.getUserId());

        return new AuthResult<>(loginUserVO, token);
    }

    @Override
    public AuthResult<LoginUserVO> whoami() {
        Long userId = requestScopeData.getUserId();

        if (userId == null) {
            throw new UnauthorizedException("用户ID异常");
        }

        try {
            User user = userMapper.findById(userId);
            if (user == null) {
                throw new NotFoundException("用户不存在");
            }

            String newToken = jwtUtil.generateToken(user.getUserId());

            LoginUserVO loginUserVO = new LoginUserVO();
            BeanUtils.copyProperties(user, loginUserVO);
            userMapper.updateLastLoginAt(user.getUserId());

            return new AuthResult<>(loginUserVO, newToken);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException("系统错误");
        }
    }

    @Override
    public UserVO getUserInfo(Long userId) {
        User user = userMapper.findById(userId);

        if (user == null) {
            throw new NotFoundException("用户不存在");
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        return userVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @NeedLogin
    public LoginUserVO updateUserInfo(UpdateUserRequest request) {
        Long userId = requestScopeData.getUserId();

        if (userId == null) {
            throw new UnauthorizedException("用户ID异常");
        }

        User user = new User();
        BeanUtils.copyProperties(request, user);
        user.setUserId(userId);

        try {
            userMapper.update(user);

            User fullUser = userMapper.findById(userId);
            syncUserToEs(fullUser);

            LoginUserVO loginUserVO = new LoginUserVO();
            BeanUtils.copyProperties(fullUser, loginUserVO);
            return loginUserVO;
        } catch (Exception e) {
            throw new BaseException("更新用户信息失败");
        }
    }

    @Override
    public AvatarVO uploadAvatar(MultipartFile file) {
        try {
            String url = fileService.uploadImage(file);
            AvatarVO avatarVO = new AvatarVO();
            avatarVO.setUrl(url);
            return avatarVO;
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
        }
    }

    @Override
    public PageResult<List<User>> getUserList(UserQueryParams queryParam) {
        int total = userMapper.countUsersByQueryParam(queryParam);
        int offset = PaginationUtils.calculateOffset(queryParam.getPage(), queryParam.getPageSize());
        Pagination pagination = new Pagination(queryParam.getPage(), queryParam.getPageSize(), total);

        try {
            List<User> users = userMapper.findByQueryParam(queryParam, queryParam.getPageSize(), offset);
            return new PageResult<>(users, pagination);
        } catch (Exception e) {
            throw new BaseException(e.getMessage());
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

    private void syncUserToEs(User user) {
        try {
            if (user == null || user.getUserId() == null) {
                return;
            }
            UserDocument doc = new UserDocument();
            BeanUtils.copyProperties(user, doc);
            userSearchRepository.save(doc);
        } catch (Exception e) {
            log.warn("同步用户到ES失败，userId={}", user != null ? user.getUserId() : null, e);
        }
    }
}
