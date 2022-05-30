package com.zc.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zc.common.utils.PageUtils;
import com.zc.gulimall.product.entity.SkuInfoEntity;
import com.zc.gulimall.product.entity.vo.SkuItemVo;

import java.util.List;
import java.util.Map;

/**
 * sku信息
 *
 * @author zhaocan
 * @email zc1872751113@gmail.com
 * @date 2022-05-02 15:28:09
 */
public interface SkuInfoService extends IService<SkuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils getSkuByCondition(Map<String, Object> params);

    List<SkuInfoEntity> getSkuBySpuId(Long spuId);

    /**
     * 查询商品的详情
     * @param skuId
     * @return
     */
    SkuItemVo item(Long skuId);
}

