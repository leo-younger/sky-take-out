package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;

    /**
     * 微信登录
     *
     * @param userLoginDTO 微信登录DTO
     * @return 微信用户实体
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO)
        {
        //获取openid
        String openid = getOpenid(userLoginDTO.getCode());

        //判断openid是否为空，如果为空则抛出异常
        if (openid == null || openid.isEmpty()) {
            throw new LoginFailedException("openid不能为空");
        }
        //根据openid查询是否为新用户
        User user = userMapper.selectByOpenid(openid);
        //如果是新用户，自动完成注册
        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }
        //返回微信用户实体
        return user;
        }

        /**
         * 获取openid
         * @param code 微信登录凭证
         * @return 微信用户openid
         */
    private String getOpenid(String code)
        {
        Map<String, String> map = new HashMap<>();
        map.put("appid", weChatProperties.getAppid());
        map.put("secret", weChatProperties.getSecret());
        map.put("js_code", code);
        map.put("grant_type", "authorization_code");
        //调用微信登录接口
        String response = HttpClientUtil.doGet(WX_LOGIN_URL, map);
        //解析response，获取openid
        return JSON.parseObject(response).getString("openid");
        }
}
