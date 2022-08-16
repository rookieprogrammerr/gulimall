package com.zc.gulimall.ware.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zc.common.to.SkuHasStockVO;
import com.zc.common.to.mq.OrderTo;
import com.zc.common.to.mq.StockLockedTo;
import com.zc.common.utils.PageUtils;
import com.zc.gulimall.ware.entity.WareSkuEntity;
import com.zc.gulimall.ware.entity.vo.LockStockResult;
import com.zc.gulimall.ware.entity.vo.WareSkuLockVo;

import java.util.List;
import java.util.Map;

/**
 * 商品库存
 *
 * @author zhaocan
 * @email zc1872751113@gmail.com
 * @date 2020-07-16 11:49:12
 */
public interface WareSkuService extends IService<WareSkuEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryByCondition(Map<String, Object> params);

    void addStock(Long skuId, Long wareId, Integer skuNum);

    List<SkuHasStockVO> getSkuHasStock(List<Long> skuIds);

    /**
     * 为某个订单锁定库存
     * @param vo
     * @return
     */
    Boolean orderLockStock(WareSkuLockVo vo);

    /**
     * 解锁库存
     * @param to
     */
    void unlockStock(StockLockedTo to);

    /**
     * 订单关闭解锁库存（重载方法）
     * @param orderTo
     */
    void unlockStock(OrderTo orderTo);
}

