package com.sky.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DailyTurnoverDTO implements Serializable {

    /**
     * 订单日期
     */
    private LocalDate orderDate;

    /**
     * 当日营业额
     */
    private BigDecimal dailyAmount;
}