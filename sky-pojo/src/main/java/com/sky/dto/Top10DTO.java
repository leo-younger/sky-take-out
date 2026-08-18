package com.sky.dto;

import lombok.Data;

/**
 * 存放数据库查询返回的top10订单名字和数量
 * @author Young
 */
@Data
public class Top10DTO {

    /**
     * 菜品或套餐名字
     */
    private String name;
    /**
     * 数量
     */
    private Long number;
}
