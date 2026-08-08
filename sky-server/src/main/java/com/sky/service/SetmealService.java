package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;

/**
 * @author Young
 */
public interface SetmealService {

    /**
     * 新增套餐
     *
     * @param setmealDTO 新增菜品信息
     */
    void save(SetmealDTO setmealDTO);

    /**
     * 套餐分页查询
     *
     * @param setmealPageQueryDTO 分页查询参数
     * @return 分页结果
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 根据套餐id查询套餐详情
     *
     * @param id 套餐id
     * @return 套餐详情
     */
    SetmealVO getById(Long id);

    /**
     * 批量删除套餐
     *
     * @param ids 套餐id列表
     */
    void delete(List<Long> ids);

    /**
     * 修改套餐
     *
     * @param setmealDTO 修改套餐信息
     */
    void update(SetmealDTO setmealDTO);

    /**
     * 修改套餐起售停售状态
     *
     * @param status 状态码
     */
    void updateStatus(Integer status,Long id);
}
