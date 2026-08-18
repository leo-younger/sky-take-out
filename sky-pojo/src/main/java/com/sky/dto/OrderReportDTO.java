package com.sky.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 用于接收订单统计数据的DTO
 * @author Young
 */
@Data
public class OrderReportDTO {
    /**
     * 订单日期
     */
    private LocalDate orderDate;

    /**
     * 订单总数
     */
    private Long orderCount;
}