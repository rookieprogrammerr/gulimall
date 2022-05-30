package com.zc.gulimall.product.service.impl;

import com.zc.gulimall.product.entity.vo.SkuItemSaleAttrsVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zc.common.utils.PageUtils;
import com.zc.common.utils.Query;

import com.zc.gulimall.product.dao.SkuSaleAttrValueDao;
import com.zc.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.zc.gulimall.product.service.SkuSaleAttrValueService;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<SkuSaleAttrValueEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 通过spuid获取所有的销售属性
     * @param spuId
     * @return
     */
    @Override
    public List<SkuItemSaleAttrsVo> getSaleAttrsBySpuId(Long spuId) {
        List<SkuItemSaleAttrsVo> saleAttrsVos = this.baseMapper.getSaleAttrsBySpuId(spuId);
        return saleAttrsVos;
    }

}