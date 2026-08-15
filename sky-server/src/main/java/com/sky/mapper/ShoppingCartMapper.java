package com.sky.mapper;

import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @author Young
 */

@Mapper
public interface ShoppingCartMapper {

    /**
     * 根据条件查询购物车
     * @param cart 购物车对象
     * @return 购物车列表
     */
    List<ShoppingCart> list(ShoppingCart cart);

    /**
     * 根据id更新数量
     * @param shoppingCart 购物车对象
     */
    @Update("update shopping_cart set number = #{number} where id = #{id}")
    void updateNumberById(ShoppingCart shoppingCart);

    /**
     * 添加购物车
     * @param list 购物车对象列表
     */
    void insertBatch(List<ShoppingCart> list);

    /**
     * 清空购物车
     * @param currentId 当前用户id
     */
    @Delete("delete from shopping_cart where user_id = #{currentId}")
    void clean(Long currentId);

    /**
     * 根据id删除购物车菜品
     * @param id 购物车id
     */
    @Delete("delete from shopping_cart where id = #{id}")
    void deleteById(Long id);

    /**
     * 根据id减少数量
     * @param id 购物车id
     */
    @Update("update shopping_cart set number = number - 1 where id = #{id}")
    void subNumberById(Long id);
}
