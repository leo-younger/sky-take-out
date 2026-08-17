package com.sky.mapper;

import com.sky.dto.UserCountDTO;
import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * @author Young
 */
@Mapper
public interface UserMapper {
    /**
     * 根据openid查询用户
     * @param openid 微信用户openid
     * @return 微信用户实体
     */
    @Select("select * from user where openid = #{openid}")
    User selectByOpenid(String openid);

    /**
     * 插入用户
     * @param user 微信用户实体
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into user (openid, name, phone, sex, id_number, avatar, create_time) " +
            "values (#{openid},#{name},#{phone},#{sex},#{idNumber},#{avatar},#{createTime})")
    void insert(User user);

    /**
     * 根据条件统计用户数量
     * @param map 查询条件
     * @return 用户数量
     */
    List<UserCountDTO> countByMap(Map<String, Object> map);

    /**
     * 统计指定时间之前注册的用户数量
     * @param beginTime 指定时间
     * @return 用户数量
     */
    @Select("select count(*) from user where create_time < #{beginTime}")
    Long countBeforeTime(LocalDateTime beginTime);
}