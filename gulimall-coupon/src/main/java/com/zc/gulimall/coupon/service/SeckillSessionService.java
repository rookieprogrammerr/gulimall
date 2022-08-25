package com.zc.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zc.common.utils.PageUtils;
import com.zc.gulimall.coupon.entity.SeckillSessionEntity;

import java.util.List;
import java.util.Map;

/**
 * 秒杀活动场次
 *
 * @author zhaocan
 * @email zc1872751113@gmail.com
 * @date 2020-07-16 15:15:16
 */
public interface SeckillSessionService extends IService<SeckillSessionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 查询最近三天需要参与秒杀的活动
     * @return
     */
    List<SeckillSessionEntity> getLates3DaySession();
}

