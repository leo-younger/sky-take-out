package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;


/**
 * @author Young
 * 用于定时修改订单数据
 */
@Slf4j
@Component
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单
     */
    @Scheduled(cron = "0 0/1 * * * ?")
    public void processTimeoutOrders()
        {
        log.info("处理超时订单");
        //获取超时订单
        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
        List<Orders> list = orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT, time);
        //遍历订单列表，将订单状态改为为已取消，设置取消时间为当前时间，取消原因为订单超时未支付，系统自动取消
        for (Orders orders : list)
        {
            orders.setStatus(Orders.CANCELLED);
            orders.setCancelTime(LocalDateTime.now());
            orders.setCancelReason("订单超时未支付，系统自动取消");
            orderMapper.update(orders);
        }
        }

    /**
     * 处理一直处在配送中的订单
     */
    @Scheduled(cron = "0 0 1 * * ?")
    //@Scheduled(cron = "1/5 * * * * ?")
    public void processDeliveryOrders()
        {
        log.info("处理一直处在配送中的订单");
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        List<Orders> list = orderMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS, time);
        if (list != null && !list.isEmpty())
        {
            for (Orders orders : list)
            {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
        }
}
