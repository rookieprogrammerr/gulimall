package com.zc.gulimall.product.service;

import com.zc.common.utils.PageUtils;
import com.zc.gulimall.product.entity.BrandEntity;
import com.zc.gulimall.product.entity.CategoryBrandRelationEntity;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 品牌分类关联
 *
 * @author zhaocan
 * @email zc1872751113@gmail.com
 * @date 2022-05-02 15:28:09
 */
public interface CategoryBrandRelationService extends IService<CategoryBrandRelationEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryBrandRelationEntity> getBrandCateRelation(Long brandId);

    void saveIdAndName(CategoryBrandRelationEntity categoryBrandRelation);

    void updateCategory(Long catId, String name);

    List<BrandEntity> getBrandByCatlogId(Long catId);
}

