package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
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
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    /**
     * 提交订单
     *
     * @param ordersSubmitDTO 订单提交需要的动词数据
     * @return 订单确认页面需要的动词数据
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderSubmitVO submit(OrdersSubmitDTO ordersSubmitDTO)
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
        //5. 封装VO返回结果
        return OrderSubmitVO.builder()
                .id(orders.getId())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .orderTime(orders.getOrderTime())
                .build();

        }

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
     * 取消订单
     *
     * @param id 订单id
     */
    @Override
    public void cancel(Long id) throws Exception
        {
        Orders orders = orderMapper.getById(id);
        Integer status = orders.getStatus();
        // 订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if (status == null)
        {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }
        if (status > Orders.TO_BE_CONFIRMED)
        {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        if (status.equals(Orders.TO_BE_CONFIRMED))
        {
            WeChatPayUtil weChatPayUtil = new WeChatPayUtil();
            //调用微信支付退款接口
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
}