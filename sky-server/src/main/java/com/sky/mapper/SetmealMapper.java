package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     *
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);

    /**
     * 新增套餐
     *
     * @param setmeal 套餐数据
     */
    @AutoFill(value = OperationType.INSERT)
    void save(Setmeal setmeal);

    /**
     * 分页查询
     *
     * @param setmealPageQueryDTO 分页查询参数
     * @return 查询到的套餐数据
     */
    List<Setmeal> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 根据套餐id查询套餐详情
     *
     * @param id 套餐id
     * @return 套餐详情
     */
    SetmealVO selectByPrimaryKey(Long id);

    /**
     * 批量删除套餐
     * @param ids 套餐id列表
     */
    void deleteBatch(List<Long> ids);

    /**
     * 更新套餐
     * @param setmeal 套餐数据
     */
    @AutoFill(value = OperationType.UPDATE)
    void updateByPrimaryKey(Setmeal setmeal);

    /**
     * 修改套餐起售停售状态
     * @param setmeal 套餐数据
     */
    @AutoFill(value = OperationType.UPDATE)
    @Update("update setmeal set status = #{status},update_time = #{updateTime},update_user = #{updateUser} where id = #{id}")
    void updateStatus(Setmeal setmeal);
}
