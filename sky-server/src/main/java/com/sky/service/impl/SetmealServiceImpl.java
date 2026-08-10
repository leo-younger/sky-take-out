package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Young
 */
@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    SetmealDishMapper setmealDishMapper;

    /**
     * 新增套餐
     *
     * @param setmealDTO 新增菜品信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(SetmealDTO setmealDTO)
        {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //先添加套餐基础信息
        setmealMapper.save(setmeal);
        Long setmealId = setmeal.getId();

        //获取套餐菜品信息并添加套餐id
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });
        //添加套餐菜品信息
        setmealDishMapper.insertBatch(setmealDishes);
        }

    /**
     * 套餐分页查询
     *
     * @param setmealPageQueryDTO 分页查询参数
     * @return 分页结果
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO)
        {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        List<Setmeal> setmealList = setmealMapper.pageQuery(setmealPageQueryDTO);
        Page<Setmeal> page = (Page<Setmeal>) setmealList;
        return new PageResult(page.getTotal(), page.getResult());
        }

    /**
     * 根据套餐id查询套餐详情
     *
     * @param id 套餐id
     * @return 套餐详情
     */
    @Override
    public SetmealVO getById(Long id)
        {
        SetmealVO setmealVO = setmealMapper.selectByPrimaryKey(id);
        //根据套餐id查询套餐菜品信息
        List<SetmealDish> setmealDishes = setmealDishMapper.selectBySetmealId(id);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
        }

    /**
     * 批量删除套餐
     *
     * @param ids 套餐id列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(List<Long> ids)
        {
        //起售中的套餐不能删除
        ids.forEach(id -> {
            SetmealVO setmealVO = setmealMapper.selectByPrimaryKey(id);
            if (setmealVO.getStatus().equals(StatusConstant.ENABLE)) {
                throw new DeletionNotAllowedException("起售中的套餐不能删除");
            }
        });
        //先删除套餐菜品关联
        setmealDishMapper.deleteBatch(ids);
        //删除套餐基础信息
        setmealMapper.deleteBatch(ids);
        }

    /**
     * 修改套餐
     *
     * @param setmealDTO 修改套餐信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SetmealDTO setmealDTO)
        {
        //先更新套餐基础信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.updateByPrimaryKey(setmeal);
        //更新套餐菜品关联
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmeal.getId());
        });
        setmealDishMapper.deleteBatch(Collections.singletonList(setmeal.getId()));
        setmealDishMapper.insertBatch(setmealDishes);
        }

    /**
     * 修改套餐起售停售状态
     *
     * @param status 状态码
     */
    @Override
    public void updateStatus(Integer status, Long id)
        {
        //要起售套餐先判断关联菜品是否有停售状态的，有则提示“套餐内包含未启售菜品，无法启售”
        if (status.equals(StatusConstant.ENABLE)) {
            //select count(*) from setmeal_dish s left join dish d on s.dish_id = d.id where d.status = 0 and s.setmeal_id = #{id}
            Long count = setmealDishMapper.countBySetmealId(id);
            if (count > 0) {
                throw new DeletionNotAllowedException("套餐内包含未启售菜品，无法启售");
            }
        }
        //更新套餐状态
        Setmeal setmeal = new Setmeal();
        setmeal.setId(id);
        setmeal.setStatus(status);
        setmealMapper.updateStatus(setmeal);
        }

    /**
     * 条件查询
     *
     * @param setmeal 套餐对象
     * @return 套餐列表
     */
    @Override
    public List<Setmeal> list(Setmeal setmeal)
        {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
        }

    /**
     * 根据id查询菜品选项
     *
     * @param id 套餐id
     * @return 菜品选项列表
     */
    @Override
    public List<DishItemVO> getDishItemById(Long id)
        {
        return setmealMapper.getDishItemBySetmealId(id);
        }
}
