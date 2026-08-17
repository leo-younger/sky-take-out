package com.sky.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserCountDTO {

    /**
     * 日期
     */
    private LocalDate date;
    /**
     * 当天新增用户数量
     */
    private Long count;
}
