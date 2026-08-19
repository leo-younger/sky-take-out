package com.sky.service.impl;

import com.sky.constant.StatusConstant;
import com.sky.dto.DailyTurnoverDTO;
import com.sky.dto.OrderReportDTO;
import com.sky.dto.UserCountDTO;
import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class WorkspaceServiceImpl implements WorkspaceService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 根据时间段统计营业数据
     * @param begin 开始时间
     * @param end 结束时间
     * @return 返回营业数据
     */
    @Override
    public BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end) {
        /*
          营业额：当日已完成订单的总金额
          有效订单：当日已完成订单的数量
          订单完成率：有效订单数 / 总订单数
          平均客单价：营业额 / 有效订单数
          新增用户：当日新增用户的数量
         */

        Map<String, Object> map = new HashMap<>();
        map.put("beginTime",begin);
        map.put("endTime",end);

        //查询总订单数
        List<OrderReportDTO> totalOrderCountList = orderMapper.countByMap(map);
        Integer totalOrderCount = (totalOrderCountList != null && !totalOrderCountList.isEmpty() && totalOrderCountList.get(0).getOrderCount() != null) ? totalOrderCountList.get(0).getOrderCount().intValue() : 0;

        map.put("status", Orders.COMPLETED);
        //营业额
        List<DailyTurnoverDTO> dailyTurnoverList = orderMapper.sumGroupByDate(map);
        Double turnover = (dailyTurnoverList != null && !dailyTurnoverList.isEmpty() && dailyTurnoverList.get(0).getDailyAmount() != null) ? dailyTurnoverList.get(0).getDailyAmount().doubleValue() : 0.0;

        //有效订单数
        List<OrderReportDTO> validOrderCountList = orderMapper.countByMap(map);
        Integer validOrderCount = (validOrderCountList != null && !validOrderCountList.isEmpty() && validOrderCountList.get(0).getOrderCount() != null) ? validOrderCountList.get(0).getOrderCount().intValue() : 0;

        Double unitPrice = 0.0;

        Double orderCompletionRate = 0.0;
        if(totalOrderCount != 0 && validOrderCount != 0){
            //订单完成率
            orderCompletionRate = validOrderCount.doubleValue() / totalOrderCount;
            //平均客单价
            unitPrice = turnover / validOrderCount;
        }

        //新增用户数
        List<UserCountDTO> newUsersList = userMapper.countByMap(map);
        Integer newUsers = (newUsersList != null && !newUsersList.isEmpty() && newUsersList.get(0).getCount() != null) ? newUsersList.get(0).getCount().intValue() : 0;

        return BusinessDataVO.builder()
                .turnover(turnover)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .unitPrice(unitPrice)
                .newUsers(newUsers)
                .build();
    }


    /**
     * 查询订单管理数据
     *
     * @return 返回今日订单管理数据
     */
    @Override
    public OrderOverViewVO getOrderOverView() {
        Map<String, Object> map = new HashMap<>();
        map.put("beginTime", LocalDateTime.now().with(LocalTime.MIN));
        map.put("endTime", LocalDateTime.now().with(LocalTime.MAX));
        map.put("status", Orders.TO_BE_CONFIRMED);

        //待接单
        List<OrderReportDTO> waitingOrders = orderMapper.countByMap(map);
        Integer waitingOrdersCount = (waitingOrders != null && !waitingOrders.isEmpty() && waitingOrders.get(0).getOrderCount() != null) ? waitingOrders.get(0).getOrderCount().intValue() : 0;

    //待派送
        map.put("status", Orders.CONFIRMED);
        List<OrderReportDTO> deliveredOrders = orderMapper.countByMap(map);
        Integer deliveredOrdersCount = (deliveredOrders != null && !deliveredOrders.isEmpty() && deliveredOrders.get(0).getOrderCount() != null) ? deliveredOrders.get(0).getOrderCount().intValue() : 0;
        //已完成
        map.put("status", Orders.COMPLETED);
        List<OrderReportDTO> completedOrders = orderMapper.countByMap(map);
        Integer completedOrdersCount = (completedOrders != null && !completedOrders.isEmpty() && completedOrders.get(0).getOrderCount() != null) ? completedOrders.get(0).getOrderCount().intValue() : 0;

        //已取消
        map.put("status", Orders.CANCELLED);
        List<OrderReportDTO> cancelledOrders = orderMapper.countByMap(map);
        Integer cancelledOrdersCount = (cancelledOrders != null && !cancelledOrders.isEmpty() && cancelledOrders.get(0).getOrderCount() != null) ? cancelledOrders.get(0).getOrderCount().intValue() : 0;

        //全部订单
        map.put("status", null);
        List<OrderReportDTO> allOrders = orderMapper.countByMap(map);
        Integer allOrdersCount = (allOrders != null && !allOrders.isEmpty() && allOrders.get(0).getOrderCount() != null) ? allOrders.get(0).getOrderCount().intValue() : 0;

        return OrderOverViewVO.builder()
                .waitingOrders(waitingOrdersCount)
                .deliveredOrders(deliveredOrdersCount)
                .completedOrders(completedOrdersCount)
                .cancelledOrders(cancelledOrdersCount)
                .allOrders(allOrdersCount)
                .build();
    }

    /**
     * 查询菜品总览
     *
     * @return 返回菜品总览数据
     */
    @Override
    public DishOverViewVO getDishOverView() {
        Map map = new HashMap();
        map.put("status", StatusConstant.ENABLE);
        Integer sold = dishMapper.countByMap(map);

        map.put("status", StatusConstant.DISABLE);
        Integer discontinued = dishMapper.countByMap(map);

        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    /**
     * 查询套餐总览
     *
     * @return 返回套餐总览数据
     */
    @Override
    public SetmealOverViewVO getSetmealOverView() {
        Map map = new HashMap();
        map.put("status", StatusConstant.ENABLE);
        Integer sold = setmealMapper.countByMap(map);

        map.put("status", StatusConstant.DISABLE);
        Integer discontinued = setmealMapper.countByMap(map);

        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }
}