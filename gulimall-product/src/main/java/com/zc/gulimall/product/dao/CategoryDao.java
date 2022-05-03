package com.zc.gulimall.product.dao;

import com.zc.gulimall.product.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author zhaocan
 * @email zc1872751113@gmail.com
 * @date 2022-05-02 15:28:09
 */
@Mapper
public interface CategoryDao extends BaseMapper<CategoryEntity> {
	
}
