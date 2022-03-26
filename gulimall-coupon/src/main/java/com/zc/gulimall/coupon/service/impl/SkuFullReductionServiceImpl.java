package com.zc.gulimall.coupon.service.impl;

import com.zc.common.to.MemberPrice;
import com.zc.common.to.SkuReductionTo;
import com.zc.common.utils.PageUtils;
import com.zc.common.utils.Query;
import com.zc.gulimall.coupon.dao.SkuFullReductionDao;
import com.zc.gulimall.coupon.dao.SkuLadderDao;
import com.zc.gulimall.coupon.entity.MemberPriceEntity;
import com.zc.gulimall.coupon.entity.SkuFullReductionEntity;
import com.zc.gulimall.coupon.entity.SkuLadderEntity;
import com.zc.gulimall.coupon.service.MemberPriceService;
import com.zc.gulimall.coupon.service.SkuFullReductionService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Resource
    private SkuLadderDao skuLadderDao;

    @Resource
    private MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 远程调用 保存sku满减价格等信息 所涉及表 sms_sku_full_reduction / sms_sku_ladder / sms_member_price
     *
     * @param skuReductionTo
     */
    @Transactional
    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        // 保存满减价格 sms_sku_ladder
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
        skuLadderEntity.setSkuId(skuReductionTo.getSkuId());
        skuLadderEntity.setFullCount(skuReductionTo.getFullCount());
        skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
        skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
        if(skuReductionTo.getFullCount()>0) {
            skuLadderDao.insert(skuLadderEntity);
        }

        // 保存满减信息 sms_sku_full_reduction
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        skuFullReductionEntity.setAddOther(skuReductionTo.getCountStatus());
        skuFullReductionEntity.setFullPrice(skuReductionTo.getFullPrice());
        skuFullReductionEntity.setReducePrice(skuReductionTo.getReducePrice());
        skuFullReductionEntity.setSkuId(skuReductionTo.getSkuId());
        if(skuFullReductionEntity.getFullPrice().compareTo(new BigDecimal("0"))==1) {
            baseMapper.insert(skuFullReductionEntity);
        }

        // 保存会员价格 sms_member_price
        List<MemberPrice> memberPrice = skuReductionTo.getMemberPrice();
        List<MemberPriceEntity> memberPriceEntityList = memberPrice.stream().map(item -> {
            MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
            memberPriceEntity.setAddOther(skuReductionTo.getCountStatus());
            memberPriceEntity.setMemberLevelId(item.getId());
            memberPriceEntity.setMemberLevelName(item.getName());
            memberPriceEntity.setMemberPrice(item.getPrice());
            memberPriceEntity.setSkuId(skuReductionTo.getSkuId());
            return memberPriceEntity;
        }).filter(item -> {
            return (item.getMemberPrice().compareTo(new BigDecimal("0")) == 1);
        }).collect(Collectors.toList());
        memberPriceService.saveBatch(memberPriceEntityList);
    }
}