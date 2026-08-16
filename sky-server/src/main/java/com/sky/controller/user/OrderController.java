package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userOrderController")
@RequestMapping("/user/order")
@Slf4j
@Api(tags = "用户订单相关接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 提交订单
     *
     * @param ordersSubmitDTO 订单提交需要的动词数据
     * @return 订单确认页面需要的动词数据
     */
    @ApiOperation("提交订单")
    @PostMapping("/submit")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO)
        {
        log.info("用户提交订单：{}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submit(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
        }

        /**
         * 订单查询
         *
         * @param ordersPageQueryDTO 订单查询的条件
         * @return 订单分页查询结果
         */
    @ApiOperation("历史订单查询")
    @GetMapping("historyOrders")
    public Result<PageResult> historyOrders(OrdersPageQueryDTO ordersPageQueryDTO)
        {
            log.info("查询用户历史订单：{}", ordersPageQueryDTO);
            PageResult pageResult = orderService.historyOrders(ordersPageQueryDTO);
            return Result.success(pageResult);
        }

        /**
         * 订单详情查询
         *
         * @param id 订单id
         * @return 订单详情
         */
        @ApiOperation("订单详情查询")
        @GetMapping("/orderDetail/{id}")
        public Result<OrderVO> orderDetail(@PathVariable Long id)
        {
            log.info("查询订单详情：{}", id);
            OrderVO orderVO = orderService.orderDetail(id);
            return Result.success(orderVO);
        }


        /**
         * 取消订单
         *
         * @param id 订单id
         * @return 取消结果
         */
        @ApiOperation("取消订单")
        @PutMapping("/cancel/{id}")
        public Result cancel(@PathVariable Long id) throws Exception
            {
            log.info("取消订单：{}", id);
            orderService.userCancelById(id);
            return Result.success();
        }

        /**
         * 再次下单
         *
         * @param id 订单id
         * @return 下单结果
         */
        @ApiOperation("再次下单")
        @PostMapping("repetition/{id}")
        public Result again(@PathVariable Long id)
        {
            log.info("再次下单：{}", id);
            orderService.again(id);
            return Result.success();
        }
}
