package com.zc.gulimall.coupon.dao;

import com.zc.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author zhaocan
 * @email zc1872751113@gmail.com
 * @date 2020-07-16 15:15:16
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
