package com.zc.gulimall.order.feign;

import com.zc.common.constant.GlobalServiceConstant;
import com.zc.gulimall.order.entity.vo.OrderItemVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(GlobalServiceConstant.CART_SERVICE)
public interface CartFeignService {
    /**
     * 获取用户购物车内选中的购物项
     * @return
     */
    @GetMapping("/currentUserCartItems")
    List<OrderItemVo> getCurrentUserCartItems();
}
