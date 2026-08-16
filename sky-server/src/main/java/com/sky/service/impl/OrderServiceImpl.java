package com.sky.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersCancelDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersRejectionDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.mapper.UserMapper;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.sky.utils.WeChatPayUtil;

/**
 * @author Young
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private WebSocketServer webSocketServer;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 1表示来单提醒，2表示催单提醒
     */
    private static final int COME_ORDER = 1;
    private static final int URGE_ORDER = 2;

    /**
     * 提交订单
     *
     * @param ordersSubmitDTO 订单提交需要的动词数据
     * @return 订单确认页面需要的动词数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO) throws JsonProcessingException
        {
        //1.异常处理
        //1.1 地址簿id不存在
        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        //1.2 购物车为空
        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if (list == null || list.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //2.插入一条订单数据（将地址、用户名等信息作为快照直接存入，防止后续地址簿修改影响历史订单）
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setUserId(BaseContext.getCurrentId());
        orders.setOrderTime(LocalDateTime.now());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPayStatus(Orders.UN_PAID);

        //2.1 地址簿快照：收货人、手机号、完整地址（省+市+区+详细地址拼接）
        orders.setConsignee(addressBook.getConsignee());
        orders.setPhone(addressBook.getPhone());
        String fullAddress = addressBook.getProvinceName()
                + addressBook.getCityName()
                + addressBook.getDistrictName()
                + addressBook.getDetail();
        orders.setAddress(fullAddress);


        orderMapper.insert(orders);
        //3. 批量插入多条订单明细数据
        List<OrderDetail> orderDetails = new ArrayList<>();
        for (ShoppingCart cart : list) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetails.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetails);
        //4. 清空购物车
        shoppingCartMapper.clean(BaseContext.getCurrentId());
        //5. 通过websocket像商家客户端推送消息，JSON格式，包含属性type，orderId，content
        Map<String, Object> map = new HashMap<>();
        map.put("type", 1);
        map.put("orderId", orders.getId());
        map.put("content", "订单号："+orders.getNumber());
        String json = objectMapper.writeValueAsString(map);
        webSocketServer.sendToAllClient(json);
        //6. 封装VO返回结果
        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();

        }

        /**
         * 用户端历史订单分页查询
         *
         * @param ordersPageQueryDTO 订单分页查询条件
         * @return 订单分页查询结果
         */
    @Override
    public PageResult historyOrders(OrdersPageQueryDTO ordersPageQueryDTO)
        {
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());

        // 阶段一：分页查询订单主表（total 完全正确）
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        List<Orders> orderList = orderMapper.pageQuery(ordersPageQueryDTO);
        Page<Orders> orderPage = (Page<Orders>) orderList;

        // 当前页无数据，直接返回空
        if (orderList == null || orderList.isEmpty()) {
            return new PageResult(orderPage.getTotal(), new ArrayList<>());
        }

        // 阶段二：根据当前页订单ID，批量查询所有明细（一次IN查询）
        List<Long> orderIds = orderList.stream().map(Orders::getId).collect(Collectors.toList());
        List<OrderDetail> detailList = orderDetailMapper.listByOrderIds(orderIds);

        // 按 orderId 分组，便于后续 O(1) 查找
        Map<Long, List<OrderDetail>> detailMap = detailList.stream()
                .collect(Collectors.groupingBy(OrderDetail::getOrderId));

        // 阶段三：手动组装 OrderVO（主表 + 明细列表）
        List<OrderVO> voList = orderList.stream().map(orders -> {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(orders, vo);
            vo.setOrderDetailList(detailMap.getOrDefault(orders.getId(), new ArrayList<>()));
            return vo;
        }).collect(Collectors.toList());

        return new PageResult(orderPage.getTotal(), voList);
        }

    /**
     * 根据id查询订单详情
     *
     * @param id 订单id
     * @return 订单详情
     */
    @Override
    public OrderVO orderDetail(Long id)
        {
        // 根据id查询订单
        Orders orders = orderMapper.getById(id);
        // 根据订单id查询订单明细
        List<OrderDetail> orderDetails = orderDetailMapper.listByOrderIds(Collections.singletonList(id));
        //封装VO对象
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
        }

    /**
     * 用户取消订单
     *
     * @param id 订单id
     */
    @Override
    public void userCancelById(Long id) throws Exception
        {
        Orders orders = orderMapper.getById(id);
        Integer status = orders.getStatus();
        // 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (status == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (status > Orders.TO_BE_CONFIRMED) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        if (status.equals(Orders.TO_BE_CONFIRMED)) {
            WeChatPayUtil weChatPayUtil = new WeChatPayUtil();
            //调用微信支付退款接口（未开发）
            weChatPayUtil.refund(
                    orders.getNumber(), //商户订单号
                    orders.getNumber(), //商户退款单号
                    new BigDecimal(0.01),//退款金额，单位 元
                    new BigDecimal(0.01));//原订单金额

            //支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
        }

    /**
     * 再来一单
     *
     * @param id 订单id
     */
    @Override
    public void again(Long id)
        {
        List<OrderDetail> orderDetails = orderDetailMapper.listByOrderIds(Collections.singletonList(id));
        List<ShoppingCart> shoppingCarts = new ArrayList<>();
        for (OrderDetail orderDetail : orderDetails) {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, shoppingCart);
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCarts.add(shoppingCart);
        }
        shoppingCartMapper.insertBatch(shoppingCarts);
        }

    /**
     * 条件查询订单（商家端）
     *
     * @param ordersPageQueryDTO 订单分页查询条件
     * @return 订单分页查询结果
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO)
        {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        List<Orders> ordersList = orderMapper.pageQuery(ordersPageQueryDTO);
        Page<Orders> orderPage = (Page<Orders>) ordersList;

        if (ordersList == null || ordersList.isEmpty()) {
            return new PageResult(orderPage.getTotal(), new ArrayList<>());
        }
        //最终返回vo的集合（还差dishes）
        //拿到orderids去查详细表数据
        List<Long> ids = ordersList.stream().map(Orders::getId).collect(Collectors.toList());
        //拿到orderids查询这些order的所有详细数据(一次sql查询)
        List<OrderDetail> orderDetails = orderDetailMapper.listByOrderIds(ids);
        //将list集合转换为map集合，key为order_id，让两个集合有联系
        Map<Long, List<OrderDetail>> collect = orderDetails.stream()
                .collect(Collectors.groupingBy(OrderDetail::getOrderId));
        //组装成vo
        List<OrderVO> list = ordersList.stream().map(orders -> {
            OrderVO orderVO = new OrderVO();
            List<OrderDetail> details = collect.getOrDefault(orders.getId(), new ArrayList<>());
            //拼接字符串
            String str = getDishesString(details);
            BeanUtils.copyProperties(orders, orderVO);
            orderVO.setOrderDishes(str);
            return orderVO;
        }).collect(Collectors.toList());
        return new PageResult(orderPage.getTotal(), list);
        }

    /**
     * 根据订单明细集合拼接菜品字符串
     *
     * @param details 订单菜品集合
     * @return 菜品字符串
     */
    private String getDishesString(List<OrderDetail> details)
        {
        return details.stream().map(orderDetail ->
                        orderDetail.getName() + " x" + orderDetail.getNumber())
                .collect(Collectors.joining("; "));
        }

    /**
     * 订单统计
     *
     * @return 订单统计结果
     */
    @Override
    public OrderStatisticsVO statistics()
        {
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);
        return OrderStatisticsVO.builder()
                .toBeConfirmed(toBeConfirmed)
                .confirmed(confirmed)
                .deliveryInProgress(deliveryInProgress)
                .build();
        }

    /**
     * 根据id查询订单详情
     *
     * @param id 订单id
     * @return 订单详情
     */
    @Override
    public OrderVO details(Long id)
        {
        //根据订单id查询订单信息
        Orders orders = orderMapper.getById(id);
        //根据订单id查询订单明细
        List<OrderDetail> orderDetails = orderDetailMapper.listByOrderIds(Collections.singletonList(id));
        //组装vo对象
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetails);
        //填充dishes菜品字符串
        orderVO.setOrderDishes(getDishesString(orderDetails));
        return orderVO;
        }

    /**
     * 确认订单
     *
     * @param id 订单id
     */
    @Override
    public void confirm(Long id)
        {
        //修改对应id订单状态
        Orders order = new Orders();
        order.setStatus(Orders.CONFIRMED);
        order.setId(id);
        orderMapper.update(order);
        }

    /**
     * 订单拒绝
     *
     * @param ordersRejectionDTO 订单拒绝DTO
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception
        {
        //先查询判断是否处于待接单状态
        Orders orderDb = orderMapper.getById(ordersRejectionDTO.getId());
        if (orderDb == null || !orderDb.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        //退款（未开发）
        WeChatPayUtil weChatPayUtil = new WeChatPayUtil();
        String refund = weChatPayUtil.refund(
                orderDb.getNumber(),
                orderDb.getNumber(),
                new BigDecimal(0.01),
                new BigDecimal(0.01));
        log.info("申请退款：{}", refund);
        //修改订单状态
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersRejectionDTO, orders);
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
        }

    /**
     * 订单取消
     *
     * @param ordersCancelDTO 订单取消DTO
     */
    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception
        {
        //根据id查询订单
        Orders order = orderMapper.getById(ordersCancelDTO.getId());
        //根据订单状态判断是否需要退款
        //订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (!order.getStatus().equals(Orders.PENDING_PAYMENT)) {
            //调用微信支付退款接口
            WeChatPayUtil weChatPayUtil = new WeChatPayUtil();
            String refund = weChatPayUtil.refund(
                    order.getNumber(),
                    order.getNumber(),
                    new BigDecimal(0.01),
                    new BigDecimal(0.01));
            log.info("申请退款：{}", refund);
        }
        //修改订单状态
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersCancelDTO, orders);
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
        }

    /**
     * 派送订单
     *
     * @param id 订单id
     */
    @Override
    public void delivery(Long id)
        {
        //根据id查询订单
        Orders ordersDb = orderMapper.getById(id);
        //判断是否为待派送状态，如果不是抛异常
        if (ordersDb == null || !ordersDb.getStatus().equals(Orders.CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders);
        }

    /**
     * 订单完成
     *
     * @param id 订单id
     */
    @Override
    public void complete(Long id)
        {
        //根据id查询订单状态
        Orders ordersDb = orderMapper.getById(id);
        //如果状态不是派送中则抛异常
        if (ordersDb == null || !ordersDb.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders = new Orders();
        orders.setId(id);
        orders.setStatus(Orders.COMPLETED);
        orders.setDeliveryTime(LocalDateTime.now());
        orderMapper.update(orders);
        }
}