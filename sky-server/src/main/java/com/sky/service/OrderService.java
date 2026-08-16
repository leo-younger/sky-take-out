package com.sky.service;

import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.vo.OrderStatisticsVO;
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
    void userCancelById(Long id) throws Exception;

    /**
     * 再次下单
     *
     * @param id 订单id
     */
    void again(Long id);

    /**
     * 条件查询订单
     *
     * @param ordersPageQueryDTO 订单查询参数
     * @return 订单查询结果
     */
    PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 订单统计
     * @return 订单统计结果
     */
    OrderStatisticsVO statistics();

    /**
     * 订单详情
     *
     * @param id 订单id
     * @return 订单详情
     */
    OrderVO details(Long id);

    /**
     * 确认订单
     * @param id 订单id
     */
    void confirm(Long id);

    /**
     * 拒绝订单
     *
     * @param ordersRejectionDTO 订单拒绝对象
     */
    void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception;

    /**
     * 取消订单
     *
     * @param ordersCancelDTO 订单取消对象
     */
    void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception;

    /**
     * 派送订单
     *
     * @param id 订单id
     */
    void delivery(Long id);

    /**
     * 完成订单
     *
     * @param id 订单id
     */
    void complete(Long id);
}