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
import com.sky.vo.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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
     * 导出数据
     *
     * @param response 响应
     */
    @Override
    public void export(HttpServletResponse response)
        {
        //获取近30天的日期范围
        LocalDate begin = LocalDate.now().plusDays(-30);
        LocalDate end = LocalDate.now().plusDays(-1);

        //构造从begin到end的日期列表
        List<LocalDate> dateList = new ArrayList<>();
        for (LocalDate date = begin; !date.isAfter(end); date = date.plusDays(1)) {
            dateList.add(date);
        }

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);

        //构造查询条件map，后续通过put/remove动态调整status来复用
        Map<String, Object> map = new HashMap<>();
        map.put("beginTime", beginTime);
        map.put("endTime", endTime);

        //查询每日总订单数（按日分组）
        List<OrderReportDTO> totalOrderList = orderMapper.countByMap(map);
        Map<LocalDate, Long> totalOrderByDate = totalOrderList.stream()
                .collect(Collectors.toMap(OrderReportDTO::getOrderDate, OrderReportDTO::getOrderCount));

        //查询每日新增用户数（按日分组）
        List<UserCountDTO> userCountList = userMapper.countByMap(map);
        Map<LocalDate, Long> newUsersByDate = userCountList.stream()
                .collect(Collectors.toMap(UserCountDTO::getDate, UserCountDTO::getCount));

        //加上已完成订单状态条件，查询每日营业额和有效订单数
        map.put("status", Orders.COMPLETED);
        //查询每日营业额（已完成订单，按日分组）
        List<DailyTurnoverDTO> dailyTurnoverList = orderMapper.sumGroupByDate(map);
        Map<LocalDate, BigDecimal> turnoverByDate = dailyTurnoverList.stream()
                .collect(Collectors.toMap(DailyTurnoverDTO::getOrderDate, DailyTurnoverDTO::getDailyAmount));

        //查询每日有效订单数（已完成订单，按日分组）
        List<OrderReportDTO> validOrderList = orderMapper.countByMap(map);
        Map<LocalDate, Long> validOrderByDate = validOrderList.stream()
                .collect(Collectors.toMap(OrderReportDTO::getOrderDate, OrderReportDTO::getOrderCount));

        //遍历日期列表，计算每日运营数据和汇总数据
        List<BusinessDataVO> dailyDataList = new ArrayList<>();
        double totalTurnover = 0.0;
        int totalOrderCount = 0;
        int totalValidOrderCount = 0;
        int totalNewUsers = 0;
        for (LocalDate date : dateList) {
            double turnover = turnoverByDate.getOrDefault(date, BigDecimal.ZERO).doubleValue();
            int totalOrder = totalOrderByDate.getOrDefault(date, 0L).intValue();
            int validOrder = validOrderByDate.getOrDefault(date, 0L).intValue();
            //订单完成率 = 有效订单数 / 总订单数
            double orderCompletionRate = totalOrder == 0 ? 0.0 : (double) validOrder / totalOrder;
            //平均客单价 = 营业额 / 有效订单数
            double unitPrice = validOrder == 0 ? 0.0 : turnover / validOrder;
            int newUsers = newUsersByDate.getOrDefault(date, 0L).intValue();

            //构建每日运营数据
            dailyDataList.add(BusinessDataVO.builder()
                    .turnover(turnover)
                    .validOrderCount(validOrder)
                    .orderCompletionRate(orderCompletionRate)
                    .unitPrice(unitPrice)
                    .newUsers(newUsers)
                    .build());

            //累加汇总
            totalTurnover += turnover;
            totalOrderCount += totalOrder;
            totalValidOrderCount += validOrder;
            totalNewUsers += newUsers;
        }

        //计算汇总的订单完成率和平均客单价
        double totalOrderCompletionRate = totalOrderCount == 0 ? 0.0 : (double) totalValidOrderCount / totalOrderCount;
        double totalUnitPrice = totalValidOrderCount == 0 ? 0.0 : totalTurnover / totalValidOrderCount;

        //读取Excel模版，并创建对象
        InputStream inputStream = this.getClass()
                .getClassLoader()
                .getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            //创建excel对象
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            //获取第一个sheet
            XSSFSheet sheet = excel.getSheet("Sheet1");

            //为第二行的第二个单元格设置值
            sheet.getRow(1).getCell(1).setCellValue("时间：" + begin + "至" + end);
            //为第四行第三个单元格设置营业额
            sheet.getRow(3).getCell(2).setCellValue(totalTurnover);
            //为第四行第五个单元格设置订单完成率
            sheet.getRow(3).getCell(4).setCellValue(totalOrderCompletionRate);
            //为第四行第七个单元格设置新增用户数
            sheet.getRow(3).getCell(6).setCellValue(totalNewUsers);
            //为第五行第三个单元格设置有效订单数
            sheet.getRow(4).getCell(2).setCellValue(totalValidOrderCount);
            //为第五行第五个单元格设置平均客单价
            sheet.getRow(4).getCell(4).setCellValue(totalUnitPrice);

            //填充每日明细数据，从第8行开始（row=7）
            for (int i = 0; i < dateList.size(); i++) {
                LocalDate date = dateList.get(i);
                BusinessDataVO data = dailyDataList.get(i);
                int row = 7 + i;
                sheet.getRow(row).getCell(1).setCellValue(date.toString());
                sheet.getRow(row).getCell(2).setCellValue(data.getTurnover());
                sheet.getRow(row).getCell(3).setCellValue(data.getValidOrderCount());
                sheet.getRow(row).getCell(4).setCellValue(data.getOrderCompletionRate());
                sheet.getRow(row).getCell(5).setCellValue(data.getUnitPrice());
                sheet.getRow(row).getCell(6).setCellValue(data.getNewUsers());
            }

            //通过输出流将excel文件下载到客户端浏览器
            excel.write(response.getOutputStream());
            excel.close();
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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