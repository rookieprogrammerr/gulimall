package com.zc.gulimall.seckill.controller;

import com.zc.common.utils.R;
import com.zc.gulimall.seckill.entity.to.SeckillSkuRedisTo;
import com.zc.gulimall.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Slf4j
@Controller
public class SeckillController {
    @Autowired
    private SeckillService seckillService;

    /**
     * 返回当前时间可以参与的秒杀商品信息
     * @return
     */
    @ResponseBody
    @GetMapping("/currentSeckillSkus")
    public R getCurrentSeckillSkus() {
        log.info("currentSeckillSkus正在执行。。。");
        List<SeckillSkuRedisTo> vos = seckillService.getCurrentSeckillSkus();
        return R.ok().setData(vos);
    }

    /**
     * 获取指定商品的秒杀预告信息
     * @param skuId
     * @return
     */
    @ResponseBody
    @GetMapping("/sku/seckill/{skuId}")
    public R skuSeckillInfo(@PathVariable("skuId") Long skuId) {
        SeckillSkuRedisTo to = seckillService.skuSeckillInfo(skuId);
        return R.ok().setData(to);
    }

    @GetMapping("/kill")
    public String secKill(@RequestParam("killId") String killId,
                          @RequestParam("key") String key,
                          @RequestParam("num") Integer num,
                          Model model) {

        String orderSn = seckillService.kill(killId, key, num);
        model.addAttribute("orderSn", orderSn);
        return "success";
    }
}
