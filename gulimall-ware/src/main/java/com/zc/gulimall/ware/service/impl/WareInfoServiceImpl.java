package com.zc.gulimall.ware.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.zc.common.utils.PageUtils;
import com.zc.common.utils.Query;
import com.zc.common.utils.R;
import com.zc.gulimall.ware.dao.WareInfoDao;
import com.zc.gulimall.ware.entity.WareInfoEntity;
import com.zc.gulimall.ware.entity.vo.FareVo;
import com.zc.gulimall.ware.entity.vo.MemberAddressVo;
import com.zc.gulimall.ware.feign.MemberFeignService;
import com.zc.gulimall.ware.service.WareInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;


@Service("wareInfoService")
public class WareInfoServiceImpl extends ServiceImpl<WareInfoDao, WareInfoEntity> implements WareInfoService {
    @Autowired
    private MemberFeignService memberFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                new QueryWrapper<WareInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 关键字查询
     *
     * @param params
     * @return
     */
    @Override
    public PageUtils queryByCondition(Map<String, Object> params) {
        QueryWrapper<WareInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> {
                w.like("name", key).or().like("address", key).or().eq("id", key);
            });
        }
        IPage<WareInfoEntity> page = this.page(
                new Query<WareInfoEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    /**
     * 根据用户的收货地址计算运费
     * @param addrId
     * @return
     */
    @Override
    public FareVo getFare(Long addrId) {
        FareVo fareVo = new FareVo();
        R r = memberFeignService.addrInfo(addrId);
        MemberAddressVo data = r.getData("memberReceiveAddress", new TypeReference<MemberAddressVo>(){
        });
        if(data != null) {
            String phone = data.getPhone();
            String substring = phone.substring(phone.length() - 1);
            BigDecimal fare = new BigDecimal(substring);
            fareVo.setAddress(data);
            fareVo.setFare(fare);
        }
        return fareVo;
    }

}