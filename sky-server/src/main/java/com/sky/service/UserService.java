package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;

public interface UserService {
    /**
     * 微信登录
     *
     * @param userLoginDTO 微信登录DTO
     * @return 微信用户
     */
    User wxLogin(UserLoginDTO userLoginDTO);
}
