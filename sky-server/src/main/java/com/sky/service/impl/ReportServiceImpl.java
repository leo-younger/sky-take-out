package com.sky.service.impl;

import com.sky.dto.DailyTurnoverDTO;
import com.sky.dto.UserCountDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
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
        while (!begin.equals(end))
        {
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
        for (LocalDate date : dateList)
        {
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
        while (!begin.equals(end))
        {
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
        for (LocalDate date : dateList)
        {
            Long newUserCount = userCountMap.getOrDefault(date, 0L);
            newUserList.add(newUserCount);
        }
        //获取begin前的总用户数
        Long totalUsersBeforeBegin = userMapper.countBeforeTime(beginTime);
        //统计每天的总用户数量
        List<Long> totalUserList = new ArrayList<>();
        for (LocalDate date : dateList)
        {
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
}