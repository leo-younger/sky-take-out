package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.sky.constant.MessageConstant.SETMEAL_ON_SALE;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Autowired
    DishMapper dishMapper;
    @Autowired
    DishFlavorMapper dishFlavorMapper;
    @Autowired
    SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品，同时插入菜品对应的口味数据，需要操作两张表：dish、dish_flavor
     *
     * @param dishDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveWithFlavor(DishDTO dishDTO)
        {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        //插入菜品数据
        dishMapper.insert(dish);
        //插入口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty())
        {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dish.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }
        }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO)
        {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        List<DishVO> list = dishMapper.page(dishPageQueryDTO);
        Page<DishVO> page = (Page<DishVO>) list;
        return new PageResult(page.getTotal(), page.getResult());
        }

    @Override
    public void deleteBatch(List<Long> ids)
        {
        //判断菜品是否可以删除--菜品是否在售
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == 1) {
                //当前菜品处于启售状态，不能删除
                throw new DeletionNotAllowedException(SETMEAL_ON_SALE);
            }
        }
        //判断菜品是否可以删除--菜品是否包含在套餐
        Long count = setmealDishMapper.countByDishIds(ids);
        if (count > 0) {
            //当前菜品包含在套餐，不能删除
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //删除菜品
        /*for (Long id : ids) {
            dishMapper.deleteById(id);
            //删除菜品关联的口味数据
            dishFlavorMapper.deleteByDishId(id);
        }*/
        //根据id批量删除菜品数据
        dishMapper.deleteBatch(ids);
        //根据id批量删除菜品口味数据
        dishFlavorMapper.deleteByDishIds(ids);
        }

    /**
     * 根据id查询菜品和对应的口味数据
     *
     * @param id 菜品id
     * @return 菜品数据
     */
    @Override
    public DishVO getByIdWithFlavor(Long id)
        {
        //根据id查询菜品数据
        Dish dish = dishMapper.getByIdWithFlavor(id);
        //根据菜品id查询对应口味数据
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);
        //封装对象
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(dishFlavors);
        return dishVO;
        }

    /**
     * 修改菜品
     *
     * @param dishDTO
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateWithFlavor(DishDTO dishDTO)
        {
            //修改菜品数据
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);
        //删除菜品对应口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        //插入新的口味数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty())
        {
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            dishFlavorMapper.insertBatch(flavors);
        }
        }

        /**
         * 根据分类id查询菜品
         *
         * @param categoryId 分类id
         * @return 菜品数据
         */
    @Override
    public List<Dish> list(Long categoryId)
        {
            //查询的菜品必须在起售状态下
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();

        return dishMapper.list(dish);
        }
}
