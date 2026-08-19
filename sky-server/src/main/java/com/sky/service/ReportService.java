package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

/**
 * @author Young
 */
public interface ReportService {
    /**
     * 获取营业额统计
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return 营业额统计
     */
    TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end);

    /**
     * 用户统计
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return 用户统计
     */
    UserReportVO getUserStatistics(LocalDate begin, LocalDate end);

    /**
     * 订单统计
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return 订单统计
     */
    OrderReportVO getOrdersStatistics(LocalDate begin, LocalDate end);

    /**
     * 获取菜品销量前十名
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return 菜品销量前十名
     */
    SalesTop10ReportVO getTop10(LocalDate begin, LocalDate end);

    /**
     * 导出数据
     *
     * @param response 响应
     */
    void export(HttpServletResponse response);
}
