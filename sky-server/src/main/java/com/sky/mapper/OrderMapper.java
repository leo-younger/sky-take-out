package com.sky.mapper;

import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Young
 */
@Mapper
public interface OrderMapper {

    /**
     * 插入订单数据
     *
     * @param orders 订单数据
     */
    void insert(Orders orders);

    /**
     * 根据条件查询订单（只查订单主表，用于分页）
     *
     * @param ordersPageQueryDTO 查询条件
     * @return 订单列表
     */
    List<Orders> pageQuery(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 根据ID查询订单
     *
     * @param id 订单id
      * @return 订单
     */
    @Select("SELECT * FROM orders WHERE id = #{id}")
    Orders getById(Long id);

    /**
     * 更新订单信息
     *
     * @param orders 订单数据
     */
    void update(Orders orders);

    /**
     * 根据状态统计订单数量
     *
     * @param status 订单状态
     * @return 订单数量
     */
    @Select("SELECT COUNT(*) FROM orders WHERE status = #{status}")
    Integer countStatus(Integer status);

    /**
     * 根据状态和订单时间查询订单
     *
     * @param status 订单状态
     * @param time 订单时间
     * @return 订单
     */
    @Select("SELECT * FROM orders WHERE status = #{status} AND order_time < #{time}")
    List<Orders> getByStatusAndOrderTime(Integer status, LocalDateTime time);
}