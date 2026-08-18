package com.sky.mapper;

import com.sky.dto.Top10DTO;
import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

/**
 * @author Young
 */
@Mapper
public interface OrderDetailMapper {

    /**
     * 批量插入订单明细数据
     *
     * @param orderDetails 订单明细数据列表
     */
    void insertBatch(List<OrderDetail> orderDetails);

    /**
     * 根据订单id列表批量查询订单明细
     *
     * @param orderIds 订单id列表
     * @return 订单明细列表
     */
    List<OrderDetail> listByOrderIds(List<Long> orderIds);
}