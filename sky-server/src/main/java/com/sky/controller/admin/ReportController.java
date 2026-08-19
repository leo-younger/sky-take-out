package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

/**
 * @author Young
 */
@RestController
@RequestMapping("/admin/report")
@Slf4j
@Api(tags = "统计相关接口")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 获取营业额统计
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return 营业额统计
     */
    @ApiOperation("获取营业额统计")
    @GetMapping("/turnoverStatistics")
    public Result<TurnoverReportVO> getTurnoverStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    )
        {
        log.info("获取营业额统计");
        TurnoverReportVO turnoverReportVO = reportService.getTurnoverStatistics(begin, end);
        return Result.success(turnoverReportVO);
        }

    /**
     * 获取用户统计
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return 用户统计
     */
    @ApiOperation("获取用户统计")
    @GetMapping("/userStatistics")
    public Result<UserReportVO> getUserStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    )
        {
        log.info("获取用户统计");
        UserReportVO userReportVO = reportService.getUserStatistics(begin, end);
        return Result.success(userReportVO);
        }

    /**
     * 获取订单统计
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return 订单统计
     */
    @ApiOperation("获取订单统计")
    @GetMapping("/ordersStatistics")
    public Result<OrderReportVO> getOrdersStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    )
        {
        log.info("获取订单统计");
        OrderReportVO orderReportVO = reportService.getOrdersStatistics(begin, end);
        return Result.success(orderReportVO);
        }

    /**
     * 获取菜品销量前十名
     *
     * @param begin 开始时间
     * @param end   结束时间
     * @return 菜品销量前十名
     */
    @ApiOperation("获取菜品销量前十名")
    @GetMapping("/top10")
    public Result<SalesTop10ReportVO> getTop10(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    )
        {
        log.info("获取菜品销量前十名");
        SalesTop10ReportVO salesTop10ReportVO = reportService.getTop10(begin, end);
        return Result.success(salesTop10ReportVO);
        }

    /**
     * 导出数据
     *
     * @param response 响应
     */
    @ApiOperation("导出数据")
    @GetMapping("/export")
    public void export(HttpServletResponse response)
        {
        log.info("导出数据");
        reportService.export(response);
        }
}
