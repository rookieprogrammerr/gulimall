package com.zc.gulimall.product.app;

import com.zc.common.exception.RRException;
import com.zc.common.utils.R;
import com.zc.gulimall.product.entity.CategoryEntity;
import com.zc.gulimall.product.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;


/**
 * 商品三级分类
 *
 * @author huanglin
 * @email 2465652971@qq.com
 * @date 2020-07-16 15:28:09
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 查出所有分类和子分类列表 以树型结构组成
     */
    @GetMapping("/list/tree")
    public R list() {
        List<CategoryEntity> categoryEntities = categoryService.listWithTree();
        return R.ok().put("data", categoryEntities);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    public R info(@PathVariable("catId") Long catId) {
        CategoryEntity category = categoryService.getById(catId);
        return R.ok().put("category", category);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    public R save(@RequestBody(required = false) CategoryEntity category) {
        categoryService.save(category);
        return R.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    public R update(@RequestBody(required = false) CategoryEntity category) {
        categoryService.updateById(category);
        return R.ok();
    }

    /**
     * 批量修改
     */
    @PostMapping("/updateBatch")
    public R update(@RequestBody(required = false) CategoryEntity[] category) {
        boolean result = categoryService.updateBatchById(Arrays.asList(category));
        if (!result) {
            throw new RRException("批量修改失败");
        }
        return R.ok();
    }

    /**
     * 批量删除
     */
    @PostMapping("/delete")
    public R delete(@RequestBody Long[] catIds) {
        categoryService.removeMenus(Arrays.asList(catIds));
        return R.ok();
    }

}
