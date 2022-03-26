package com.zc.gulimall.order.dao;

import com.zc.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author huanglin
 * @email 2465652971@qq.com
 * @date 2020-07-16 15:11:15
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
