package com.sky.controller.admin;


import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@Api(tags = "商家订单管理")
@RequestMapping("/admin/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 条件查询订单
     *
     * @param ordersPageQueryDTO 订单查询参数
     * @return 订单查询结果
     */
    @ApiOperation("条件查询订单")
    @GetMapping("/conditionSearch")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO)
        {
        log.info("接收到订单查询请求，参数：{}", ordersPageQueryDTO);
        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
        }

    /**
     * 订单统计
     *
     * @return 订单统计结果
     */
    @ApiOperation("订单统计")
    @GetMapping("/statistics")
    public Result<OrderStatisticsVO> statistics()
        {
        log.info("接收到订单统计请求");
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
        }

    /**
     * 订单详情
     *
     * @param id 订单id
     * @return 订单详情
     */
    @ApiOperation("订单详情")
    @GetMapping("/details/{id}")
    public Result<OrderVO> details(@PathVariable Long id)
        {
        log.info("接收到订单详情请求，参数：{}", id);
        OrderVO orderVO = orderService.details(id);
        return Result.success(orderVO);
        }

    /**
     * 确认订单
     *
     * @param ordersDTO 订单对象
     * @return 确认结果
     */
    @ApiOperation("确认订单")
    @PutMapping("/confirm")
    public Result confirm(@RequestBody OrdersDTO ordersDTO)
        {
        log.info("接收到确认订单请求，参数：{}", ordersDTO);
        orderService.confirm(ordersDTO.getId());
        return Result.success();
        }

    /**
     * 拒绝订单
     *
     * @param ordersRejectionDTO 订单拒绝对象
     * @return 拒绝结果
     */
    @ApiOperation("拒绝订单")
    @PutMapping("/rejection")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) throws Exception
        {
        log.info("接收到拒绝订单请求，参数：{}", ordersRejectionDTO);
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
        }

        /**
         * 取消订单
         *
         * @param ordersCancelDTO 订单对象
         * @return 取消结果
         */
    @ApiOperation("取消订单")
    @PutMapping("/cancel")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) throws Exception
        {
        log.info("接收到取消订单请求，参数：{}", ordersCancelDTO);
        orderService.cancel(ordersCancelDTO);
        return Result.success();
        }

        /**
         * 派送订单
         *
         * @param id 订单id
         * @return 派送结果
         */
        @ApiOperation("派送订单")
        @PutMapping("/delivery/{id}")
        public Result delivery(@PathVariable Long id)
        {
        log.info("接收到派送订单请求，参数：{}", id);
        orderService.delivery(id);
        return Result.success();
        }

        /**
         * 完成订单
         *
         * @param id 订单id
         * @return 完成结果
         */
        @ApiOperation("完成订单")
        @PutMapping("/complete/{id}")
        public Result complete(@PathVariable Long id)
        {
        log.info("接收到完成订单请求，参数：{}", id);
        orderService.complete(id);
        return Result.success();
        }
}