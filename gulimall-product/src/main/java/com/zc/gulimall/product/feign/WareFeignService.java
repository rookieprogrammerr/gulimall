package com.zc.gulimall.product.feign;

import com.zc.common.constant.GlobalServiceConstant;
import com.zc.common.to.SkuHasStockVO;
import com.zc.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(GlobalServiceConstant.WARE_SERVICE)
public interface WareFeignService {

    /**
     * 1、R设计的时候可以加上泛型
     * 2、直接返回我们想要的结果
     * 3、自己封装解析结果
     * @param skuIds
     * @return
     */
    @PostMapping("/ware/waresku/hasstock")
    //public R<List<SkuHasStockVO>> getSkuHasStock(@RequestBody List<Long> skuIds);
    public R getSkuHasStock(@RequestBody List<Long> skuIds);
}
