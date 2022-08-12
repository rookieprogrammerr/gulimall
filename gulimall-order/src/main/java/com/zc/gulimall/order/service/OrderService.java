package com.zc.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zc.common.utils.PageUtils;
import com.zc.gulimall.order.entity.OrderEntity;
import com.zc.gulimall.order.entity.vo.OrderConfirmVo;
import com.zc.gulimall.order.entity.vo.OrderSubmitVo;
import com.zc.gulimall.order.entity.vo.SubmitOrderResponseVo;

import java.util.Map;

/**
 * 订单
 *
 * @author zhaocan
 * @email zc1872751113@gmail.com
 * @date 2020-07-16 15:11:15
 */
public interface OrderService extends IService<OrderEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 订单确认页返回需要用的数据
     * @return
     */
    OrderConfirmVo confirmOrder();

    /**
     * 下单
     * @param vo
     * @return
     */
    SubmitOrderResponseVo submitOrder(OrderSubmitVo vo);
}

