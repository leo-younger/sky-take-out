package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

/**
 * @author Young
 */
public interface ShoppingCartService {
    /**
     * 添加购物车
     * @param shoppingCartDTO 购物车数据
     */
    void add(ShoppingCartDTO shoppingCartDTO);

    /**
     * 显示购物车
     * @return 购物车列表
     */
    List<ShoppingCart> showShoppingCart();

    /**
     * 清空购物车
     */
    void clean();
}
