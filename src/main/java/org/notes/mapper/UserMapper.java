package org.notes.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.notes.model.dto.user.UserQueryParams;
import org.notes.model.entity.User;

import java.util.List;

/**
 * @author Administrator
 * @description 针对表【user(用户表)】的数据库操作Mapper
 * @createDate 2026-01-27 20:20:26
 * @Entity org.notes.model.entity.User
 */
@Mapper
public interface UserMapper {

    int insert(User user);

    User findById(@Param("userId") Long userId);

    List<User> findByIdBatch(@Param("userIds") List<Long> userIds);

    User findByAccount(@Param("account") String account);

    User findByEmail(@Param("email") String email);

    List<User> findByQueryParam(@Param("queryParams") UserQueryParams queryParams,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset);

    int countUsersByQueryParam(@Param("queryParams") UserQueryParams queryParams);

    int update(User user);

    int updateLastLoginAt(@Param("userId") Long userId);

    int getTodayLoginCount();

    int getTodayRegisterCount();

    int getTotalRegisterCount();

    List<User> searchUsers(@Param("keyword") String keyword,
            @Param("limit") int limit,
            @Param("offset") int offset);

    /**
     * 查询所有用户（用于 ES 全量同步）
     *
     * @return 所有用户列表
     */
    List<User> findAll();
}
