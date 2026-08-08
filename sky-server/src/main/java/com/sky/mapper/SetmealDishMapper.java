package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
    /**
     * 根据菜品id查询是否关联套餐
     * @param dishIds 菜品 id
     * @return 套餐数量
     */
    Long countByDishIds(List<Long> dishIds);

    /**
     * 批量插入套餐和菜品的关联关系
     * @param setmealDishes 套餐和菜品的关联关系
     */
    void insertBatch(List<SetmealDish> setmealDishes);

    /**
     * 根据套餐id查询套餐和菜品的关联关系
     * @param id 套餐id
     * @return 套餐和菜品的关联关系
     */
    @Select("select * from setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> selectBySetmealId(Long id);

    /**
     * 批量删除套餐和菜品的关联关系
     * @param ids 套餐id列表
     */
    void deleteBatch(List<Long> ids);

    /**
     * 根据套餐id查询是否关联停售状态的菜品
     * @param id 套餐id
     * @return 停售状态菜品数量
     */
    Long countBySetmealId(Long id);
}
