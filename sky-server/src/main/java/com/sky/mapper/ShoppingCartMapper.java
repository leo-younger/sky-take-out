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
     * @param cart 购物车对象
     */
    @Insert("insert into shopping_cart (name, image, user_id, dish_id, setmeal_id, dish_flavor, number, amount, create_time) " +
            "values (#{name}, #{image}, #{userId}, #{dishId}, #{setmealId}, #{dishFlavor}, #{number}, #{amount}, #{createTime});")
    void add(ShoppingCart cart);

    /**
     * 清空购物车
     * @param currentId 当前用户id
     */
    @Delete("delete from shopping_cart where user_id = #{currentId}")
    void clean(Long currentId);
}
