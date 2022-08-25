package com.zc.gulimall.coupon.service.impl;

import com.zc.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.zc.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zc.common.utils.PageUtils;
import com.zc.common.utils.Query;

import com.zc.gulimall.coupon.dao.SeckillSessionDao;
import com.zc.gulimall.coupon.entity.SeckillSessionEntity;
import com.zc.gulimall.coupon.service.SeckillSessionService;
import org.springframework.util.CollectionUtils;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {
    @Autowired
    private SeckillSkuRelationService skuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<SeckillSessionEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 查询最近三天需要参与秒杀的活动
     * @return
     */
    @Override
    public List<SeckillSessionEntity> getLates3DaySession() {
        //计算最近3天的时间
        List<SeckillSessionEntity> list = this.list(new QueryWrapper<SeckillSessionEntity>().between("start_time", startTime(), endTime()));
        if(!CollectionUtils.isEmpty(list)) {
            List<SeckillSessionEntity> collect = list.stream().map(session -> {
                Long id = session.getId();
                List<SeckillSkuRelationEntity> relationEntities = skuRelationService.list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", id));
                session.setRelationSkus(relationEntities);
                return session;
            }).collect(Collectors.toList());

            return collect;
        }
        return null;
    }

    /**
     * 获取起始时间
     * @return
     */
    private String startTime() {
        LocalDate now = LocalDate.now();
        LocalTime start = LocalTime.MIN;
        return LocalDateTime.of(now, start).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 获取结束时间
     * @return
     */
    private String endTime() {
        LocalDate plusDays = LocalDate.now().plusDays(3);
        LocalTime end = LocalTime.MIN;
        return LocalDateTime.of(plusDays, end).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}