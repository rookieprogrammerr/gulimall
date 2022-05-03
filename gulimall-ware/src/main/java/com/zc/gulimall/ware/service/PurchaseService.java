package com.zc.gulimall.ware.service;

import com.zc.gulimall.ware.entity.vo.DoneReqVo;
import com.zc.gulimall.ware.entity.vo.MergeReqVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zc.common.utils.PageUtils;
import com.zc.gulimall.ware.entity.PurchaseEntity;

import java.util.List;
import java.util.Map;

/**
 * 采购信息
 *
 * @author zhaocan
 * @email zc1872751113@gmail.com
 * @date 2020-07-16 11:49:12
 */
public interface PurchaseService extends IService<PurchaseEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils getUnclaimedPurchase(Map<String, Object> params);

    void mergePurchase(MergeReqVo mergeReqVo);

    void receivePurchase(List<Long> ids);

    void done(DoneReqVo doneReqVo);
}

