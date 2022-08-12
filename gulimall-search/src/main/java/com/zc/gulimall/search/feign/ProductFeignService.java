package com.zc.gulimall.search.feign;

import com.zc.common.constant.GlobalServiceConstant;
import com.zc.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(GlobalServiceConstant.PRODUCT_SERVICE)
public interface ProductFeignService {

    /**
     * 获取指定属性信息
     * @param attrId
     * @return
     */
    @GetMapping("/product/attr/info/{attrId}")
    R attrInfo(@PathVariable("attrId") Long attrId);

    /**
     * 获取品牌信息
     */
    @GetMapping("/product/brand/infos")
    R brandsInfos(@RequestParam("brandIds") List<Long> brandIds);
}
