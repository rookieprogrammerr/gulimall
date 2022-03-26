package com.zc.gulimall.product.controller;

import com.zc.common.utils.PageUtils;
import com.zc.common.utils.R;
import com.zc.common.valid.SaveValid;
import com.zc.common.valid.UpdateStatus;
import com.zc.common.valid.UpdateVaild;
import com.zc.gulimall.product.entity.BrandEntity;
import com.zc.gulimall.product.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;


/**
 * 品牌
 *
 * @author huanglin
 * @email 2465652971@qq.com
 * @date 2020-07-16 15:28:09
 */
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @RequestMapping("/info/{brandId}")
    public R info(@PathVariable("brandId") Long brandId) {
        BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@Validated(value = SaveValid.class) @RequestBody BrandEntity brand/*BindingResult bindingResult*/) {
//        if (bindingResult.hasErrors()) {
//            Map<String, String> map = new HashMap<>(16);
//            bindingResult.getFieldErrors().forEach((item) -> {
//                // fieldError
//                // 错误消息
//                String message = item.getDefaultMessage();
//                // 错误字段
//                String field = item.getField();
//                map.put(field, message);
//            });
//            return R.error(400, "提交的数据不合法").put("data", map);
//        } else {
//        }
        brandService.save(brand);
        return R.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    public R update(@Validated(value = UpdateVaild.class) @RequestBody BrandEntity brand) {
        brandService.updateById(brand);
        return R.ok();
    }

    /**
     * 修改状态
     */
    @PostMapping("/update/status")
    public R updateStatus(@Validated(value = UpdateStatus.class) @RequestBody BrandEntity brand) {
        brandService.updateById(brand);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] brandIds) {
        brandService.removeBrandAndRelation(Arrays.asList(brandIds));
        return R.ok();
    }

}
