package com.sky.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 根据菜品id查询是否关联套餐
     * @param dishIds 菜品 id
     * @return 套餐数量
     */
    Long countByDishIds(List<Long> dishIds);
}
