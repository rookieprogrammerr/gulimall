package com.zc.gulimall.product.app;

import com.zc.common.utils.PageUtils;
import com.zc.common.utils.R;
import com.zc.gulimall.product.entity.BrandEntity;
import com.zc.gulimall.product.entity.CategoryBrandRelationEntity;
import com.zc.gulimall.product.service.CategoryBrandRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * 品牌分类关联
 *
 * @author zhaocan
 * @email zc1872751113@gmail.com
 * @date 2022-05-02 15:28:09
 */
@RestController
@RequestMapping("product/categorybrandrelation")
public class CategoryBrandRelationController {
    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;

    /**
     * 获取catId下的品牌
     */
    @GetMapping("/brands/list")
    public R getBrandByCatId(@RequestParam Long catId) {
        List<BrandEntity> list = categoryBrandRelationService.getBrandByCatlogId(catId);
        return R.ok().put("data", list);
    }

    /**
     * 获取品牌关联的分类
     */
    @GetMapping("/catelog/list")
    public R getBrandCateRelation(@RequestParam Long brandId) {
        List<CategoryBrandRelationEntity> attrBrandCateRelationVos = categoryBrandRelationService.getBrandCateRelation(brandId);
        return R.ok().put("data", attrBrandCateRelationVos);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = categoryBrandRelationService.queryPage(params);
        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        CategoryBrandRelationEntity categoryBrandRelation = categoryBrandRelationService.getById(id);

        return R.ok().put("categoryBrandRelation", categoryBrandRelation);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {
        categoryBrandRelationService.saveIdAndName(categoryBrandRelation);
        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CategoryBrandRelationEntity categoryBrandRelation) {
        categoryBrandRelationService.updateById(categoryBrandRelation);
        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        categoryBrandRelationService.removeByIds(Arrays.asList(ids));
        return R.ok();
    }
}
