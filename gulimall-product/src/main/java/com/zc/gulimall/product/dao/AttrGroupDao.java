package com.zc.gulimall.product.dao;

import com.zc.gulimall.product.entity.AttrGroupEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zc.gulimall.product.entity.vo.SkuItemVo;
import com.zc.gulimall.product.entity.vo.SpuItemBaseAttrGroupVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 属性分组
 * 
 * @author zhaocan
 * @email zc1872751113@gmail.com
 * @date 2022-05-02 15:28:09
 */
@Mapper
public interface AttrGroupDao extends BaseMapper<AttrGroupEntity> {

    List<SpuItemBaseAttrGroupVo> getAttrGroupWithAttrsBySpuId(@Param("spuId") Long spuId, @Param("catelogId") Long catelogId);
}
