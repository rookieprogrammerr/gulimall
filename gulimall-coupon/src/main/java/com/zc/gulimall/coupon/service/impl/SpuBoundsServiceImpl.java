package com.zc.gulimall.coupon.service.impl;

import com.zc.common.to.SpuBoundsTo;
import com.zc.common.utils.PageUtils;
import com.zc.common.utils.Query;
import com.zc.gulimall.coupon.dao.SpuBoundsDao;
import com.zc.gulimall.coupon.entity.SpuBoundsEntity;
import com.zc.gulimall.coupon.service.SpuBoundsService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service("spuBoundsService")
public class SpuBoundsServiceImpl extends ServiceImpl<SpuBoundsDao, SpuBoundsEntity> implements SpuBoundsService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuBoundsEntity> page = this.page(
                new Query<SpuBoundsEntity>().getPage(params),
                new QueryWrapper<SpuBoundsEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * gulimall-product远程调用保存满减信息
     *
     * @param spuBoundsTo
     */
    @Override
    public void saveSpuBounds(SpuBoundsTo spuBoundsTo) {
        SpuBoundsEntity spuBoundsEntity = new SpuBoundsEntity();
        spuBoundsEntity.setBuyBounds(spuBoundsTo.getBuyBounds());
        spuBoundsEntity.setGrowBounds(spuBoundsTo.getGrowBounds());
        spuBoundsEntity.setWork(1);
        spuBoundsEntity.setSpuId(spuBoundsTo.getSpuId());
        baseMapper.insert(spuBoundsEntity);
    }
}