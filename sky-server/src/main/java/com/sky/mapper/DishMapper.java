package com.sky.mapper;

import com.sky.annotation.AutoDeleteRedis;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     *
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    /**
     * 插入菜品数据
     *
     * @param dish
     */
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);

    /**
     * 菜品分页查询
     *
     * @return
     */
    List<DishVO> page(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 根据id查询菜品和对应的口味数据
     *
     * @param id 菜品id
     * @return 菜品对象
     */
    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);

    /**
     * 根据id删除菜品数据
     *
     * @param id 菜品id
     */
    @Delete("delete from dish where id = #{id}")
    void deleteById(Long id);

    /**
     * 批量删除菜品
     *
     * @param dishIds 菜品id集合
     */
    void deleteBatch(List<Long> dishIds);

    /**
     * 根据id查询菜品和口味数据
     *
     * @param id 菜品id
     * @return 菜品对象
     */
    @Select("select * from dish where id = #{id}")
    Dish getByIdWithFlavor(Long id);

    /**
     * 修改菜品数据
     *
     * @param dish 菜品对象
     */
    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 根据分类id查询菜品
     *
     * @param dish 菜品对象(包含状态和分类id)
     * @return 菜品列表
     */
    List<Dish> list(Dish dish);

    /**
     * 修改菜品起售停售状态
     *
     * @param dish 菜品对象
     */
    @AutoFill(value = OperationType.UPDATE)
    @Update("update dish set status = #{status} where id = #{id}")
    void updateStatus(Dish dish);

    /**
     * 根据条件统计菜品数量
     *
     * @param map 查询条件
     * @return 菜品数量
     */
    Integer countByMap(Map map);
}
