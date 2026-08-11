package com.sky.service;

import com.sky.dto.ShoppingCartDTO;

/**
 * @author Young
 */
public interface ShoppingCartService {
    /**
     * 添加购物车
     * @param shoppingCartDTO 购物车数据
     */
    void add(ShoppingCartDTO shoppingCartDTO);
}
