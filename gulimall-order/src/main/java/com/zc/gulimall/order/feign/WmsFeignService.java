package com.zc.gulimall.order.feign;

import com.zc.common.constant.GlobalServiceConstant;
import com.zc.common.utils.R;
import com.zc.gulimall.order.entity.vo.WareSkuLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(GlobalServiceConstant.WARE_SERVICE)
public interface WmsFeignService {
    /**
     * 查询sku是否有库存
     */
    @PostMapping("/ware/waresku/hasstock")
    R getSkuHasStock(@RequestBody List<Long> skuIds);
    /**
     * 获取运费信息
     * @param addrId
     * @return
     */
    @GetMapping("/ware/wareinfo/fare")
    R getFare(@RequestParam("addrId") Long addrId);
    /**
     * 锁定库存
     * @param vo
     * @return
     */
    @PostMapping("/ware/waresku/lock/order")
    R orderLockStock(@RequestBody WareSkuLockVo vo);
}
