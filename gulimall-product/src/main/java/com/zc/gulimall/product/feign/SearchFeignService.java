package com.zc.gulimall.product.feign;

import com.zc.common.constant.GlobalServiceConstant;
import com.zc.common.to.es.SkuEsModel;
import com.zc.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(GlobalServiceConstant.SEARCH_SERVICE)
public interface SearchFeignService {

    @PostMapping("/search/save/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels);
}
