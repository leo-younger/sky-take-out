package com.sky.service.impl;

import com.sky.dto.DailyTurnoverDTO;
import com.sky.dto.OrderReportDTO;
import com.sky.dto.Top10DTO;
import com.sky.dto.UserCountDTO;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Young
 */
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    /**
     * 获取营业额统计
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return 营业额统计
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end)
        {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        LocalDateTime beginTime = LocalDateTime.of(dateList.get(0), LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(dateList.get(dateList.size() - 1), LocalTime.MAX);
        Map<String, Object> map = new HashMap<>();
        map.put("beginTime", beginTime);
        map.put("endTime", endTime);
        map.put("status", Orders.COMPLETED);

        List<DailyTurnoverDTO> dailyTurnoverList = orderMapper.sumGroupByDate(map);
        Map<LocalDate, BigDecimal> turnoverMap = dailyTurnoverList.stream()
                .collect(Collectors.toMap(DailyTurnoverDTO::getOrderDate, DailyTurnoverDTO::getDailyAmount));

        List<BigDecimal> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            BigDecimal turnover = turnoverMap.getOrDefault(date, BigDecimal.ZERO);
            turnoverList.add(turnover);
        }
        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
        }

    /**
     * 用户统计
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return 用户统计
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end)
        {
        //存放从begin到end的所有日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        LocalDateTime beginTime = LocalDateTime.of(dateList.get(0), LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(dateList.get(dateList.size() - 1), LocalTime.MAX);
        Map<String, Object> map = new HashMap<>();
        map.put("beginTime", beginTime);
        map.put("endTime", endTime);
        //统计当天新增用户数量
        List<UserCountDTO> userCountList = userMapper.countByMap(map);
        //将统计结果放入map中，方便后续获取
        Map<LocalDate, Long> userCountMap = userCountList.stream()
                .collect(Collectors.toMap(UserCountDTO::getDate, UserCountDTO::getCount));
        //统计每天新增用户数量
        List<Long> newUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            Long newUserCount = userCountMap.getOrDefault(date, 0L);
            newUserList.add(newUserCount);
        }
        //获取begin前的总用户数
        Long totalUsersBeforeBegin = userMapper.countBeforeTime(beginTime);
        //统计每天的总用户数量
        List<Long> totalUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            Long totalUsers = userCountMap.getOrDefault(date, 0L) + totalUsersBeforeBegin;
            totalUsersBeforeBegin = totalUsers;
            totalUserList.add(totalUsers);
        }
        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .build();
        }

    /**
     * 订单统计
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return 订单统计
     */
    @Override
    public OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end)
        {
        //存放从begin到end的所有日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        //统计当天订单总数量
        Map<String, Object> map = new HashMap<>();
        map.put("beginTime", LocalDateTime.of(dateList.get(0), LocalTime.MIN));
        map.put("endTime", LocalDateTime.of(dateList.get(dateList.size() - 1), LocalTime.MAX));
        List<Integer> orderCountList = countOrderByMap(map, dateList);
        //统计当天有效订单总数量
        map.put("status", Orders.COMPLETED);
        List<Integer> validOrderCountList = countOrderByMap(map, dateList);
        //计算总订单数和有效订单数
        int totalOrderCount = orderCountList.stream().reduce(0, Integer::sum);
        int validOrderCount = validOrderCountList.stream().reduce(0, Integer::sum);
        //计算订单完成率
        double orderCompletionRate = totalOrderCount == 0 ? 0.0 : (double) validOrderCount / totalOrderCount;
        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
        }

    /**
     * 获取销量前十的菜品
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return 销量前十的菜品
     */
    @Override
    public SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end)
        {
        //通过内连接查询订单表和订单明细表，拿到订单ID对应的菜品ID和数量，以及菜品名称
        Map<String, Object> map = new HashMap<>();
        map.put("beginTime", LocalDateTime.of(begin, LocalTime.MIN));
        map.put("endTime", LocalDateTime.of(end, LocalTime.MAX));
        map.put("status", Orders.COMPLETED);
        List<Top10DTO> top10DTOList = orderMapper.getTop10(map);
        //遍历top10列表存进VO的name列表和number列表
        List<String> nameList = new ArrayList<>();
        List<Integer> numberList = new ArrayList<>();
        for (Top10DTO top10DTO : top10DTOList) {
            nameList.add(top10DTO.getName());
            numberList.add(top10DTO.getNumber() == null ? 0 : top10DTO.getNumber().intValue());
        }
        //返回结果
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList, ","))
                .numberList(StringUtils.join(numberList, ","))
                .build();
        }

    /**
     * 根据map中的条件统计订单数量
     *
     * @param map      条件
     * @param dateList 日期列表
     * @return 订单数量列表
     */
    private List<Integer> countOrderByMap(Map<String, Object> map, List<LocalDate> dateList)
        {
        List<OrderReportDTO> count = orderMapper.countByMap(map);
        Map<LocalDate, Long> countMap = count.stream()
                .collect(Collectors.toMap(OrderReportDTO::getOrderDate, OrderReportDTO::getOrderCount));
        List<Integer> orderCountList = new ArrayList<>();
        for (LocalDate date : dateList) {
            int orderCount = countMap.getOrDefault(date, 0L).intValue();
            orderCountList.add(orderCount);
        }
        return orderCountList;
        }
}