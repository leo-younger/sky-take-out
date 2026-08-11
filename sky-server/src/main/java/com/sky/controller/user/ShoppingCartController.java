package com.sky.controller.user;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@Api(tags = "C端购物车相关接口")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     *
     * @param shoppingCartDTO 购物车数据
     */
    @ApiOperation(value = "添加购物车")
    @PostMapping("/add")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO)
        {
        log.info("购物车添加");
        shoppingCartService.add(shoppingCartDTO);
        return Result.success();
        }

        /**
         * 查询购物车
         * @return 购物车列表
         */
    @ApiOperation(value = "查询购物车")
    @GetMapping("/list")
    public Result<List<ShoppingCart>> list()
        {
        log.info("购物车列表");
        List<ShoppingCart> shoppingCartList = shoppingCartService.showShoppingCart();
        return Result.success(shoppingCartList);
        }

    /**
     * 清空购物车
      * @return Result 结果
     */
    @ApiOperation(value = "清空购物车")
    @DeleteMapping("/clean")
    public Result clean()
        {
        log.info("购物车清空");
        shoppingCartService.clean();
        return Result.success();
        }
}
