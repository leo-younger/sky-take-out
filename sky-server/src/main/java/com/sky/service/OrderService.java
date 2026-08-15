package com.sky.service;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

/**
 * @author Young
 */
public interface OrderService {

    /**
     * 提交订单
     *
     * @param ordersSubmitDTO 订单提交需要的动词数据
     * @return 订单确认页面需要的动词数据
     */
    OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO);

    /**
     * 订单查询
     *
     * @param ordersPageQueryDTO 订单查询的条件
     * @return 订单分页查询结果
     */
    PageResult historyOrders(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 订单详情查询
     *
     * @param id 订单id
     * @return 订单详情
     */
    OrderVO orderDetail(Long id);

    /**
     * 取消订单
     *
     * @param id 订单id
     */
    void cancel(Long id) throws Exception;

    /**
     * 再次下单
     *
     * @param id 订单id
     */
    void again(Long id);
}
