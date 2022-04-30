package com.zc.gulimall.ware.controller;

import com.zc.common.to.SkuHasStockVO;
import com.zc.common.utils.PageUtils;
import com.zc.common.utils.R;
import com.zc.gulimall.ware.entity.WareSkuEntity;
import com.zc.gulimall.ware.service.WareSkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 商品库存
 *
 * @author huanglin
 * @email 2465652971@qq.com
 * @date 2020-07-16 11:49:12
 */
@RestController
@RequestMapping("ware/waresku")
public class WareSkuController {
    @Autowired
    private WareSkuService wareSkuService;
    
    //查询sku是否有库存
    @PostMapping("/hasstock")
    public R getSkuHasStock(@RequestBody List<Long> skuIds) {
    //public R<List<SkuHasStockVO>> getSkuHasStock(@RequestBody List<Long> skuIds) {
        //sku_id、stock
        List<SkuHasStockVO> vos = wareSkuService.getSkuHasStock(skuIds);

        //return ok;
        return R.ok().setData(vos);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = wareSkuService.queryByCondition(params);
        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        WareSkuEntity wareSku = wareSkuService.getById(id);

        return R.ok().put("wareSku", wareSku);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.save(wareSku);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody WareSkuEntity wareSku) {
        wareSkuService.updateById(wareSku);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        wareSkuService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}