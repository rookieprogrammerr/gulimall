package com.zc.gulimall.order.feign;

import com.zc.common.constant.GlobalServiceConstant;
import com.zc.gulimall.order.entity.vo.MemberAddressVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(GlobalServiceConstant.MEMBER_SERVICE)
public interface MemberFeignService {
    /**
     * 返回指定会员的所有收货地址
     * @param memberId
     * @return
     */
    @GetMapping("/member/memberreceiveaddress/{memberId}/addresses")
    List<MemberAddressVo> getAddress(@PathVariable("memberId") Long memberId);
}
