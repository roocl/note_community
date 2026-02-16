package org.notes.service;

import org.notes.model.base.AuthResult;
import org.notes.model.base.PageResult;
import org.notes.model.dto.user.LoginRequest;
import org.notes.model.dto.user.RegisterRequest;
import org.notes.model.dto.user.UpdateUserRequest;
import org.notes.model.dto.user.UserQueryParams;
import org.notes.model.entity.User;
import org.notes.model.vo.user.AvatarVO;
import org.notes.model.vo.user.LoginUserVO;
import org.notes.model.vo.user.RegisterVO;
import org.notes.model.vo.user.UserVO;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Transactional
public interface UserService {
    AuthResult<RegisterVO> register(RegisterRequest request);

    AuthResult<LoginUserVO> login(LoginRequest request);

    AuthResult<LoginUserVO> whoami();

    UserVO getUserInfo(Long userId);

    LoginUserVO updateUserInfo(UpdateUserRequest request);

    AvatarVO uploadAvatar(MultipartFile file);

    PageResult<List<User>> getUserList(UserQueryParams queryParam);

    Map<Long, User> getUserMapByIds(List<Long> authorIds);
}
