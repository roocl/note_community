package org.notes.service;

import org.notes.model.base.ApiResponse;
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

@Transactional
public interface UserService {
    public ApiResponse<RegisterVO> register(RegisterRequest request);

    public ApiResponse<LoginUserVO> login(LoginRequest request);

    public ApiResponse<LoginUserVO> whoami();

    public ApiResponse<UserVO> getUserInfo(Long userId);

    public ApiResponse<LoginUserVO> updateUserInfo(UpdateUserRequest request);

    public ApiResponse<AvatarVO> uploadAvatar(MultipartFile file);

    public ApiResponse<List<User>> getUserList(UserQueryParams queryParam);
}
