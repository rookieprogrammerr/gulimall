package com.zc.gulimall.product.service;

import com.zc.common.utils.PageUtils;
import com.zc.gulimall.product.entity.AttrEntity;
import com.zc.gulimall.product.entity.AttrGroupEntity;
import com.zc.gulimall.product.entity.vo.AttrGroupAndAttrVo;
import com.zc.gulimall.product.entity.vo.AttrGroupRelationVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author zhaocan
 * @email zc1872751113@gmail.com
 * @date 2022-05-02 15:28:09
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPageByCid(Map<String, Object> params, Long catelogId);

    List<AttrEntity> getAttrByAttrGroupId(Long attrgroupId);

    void deleteRelation(AttrGroupRelationVo[] attrGroupRelationVo);

    PageUtils getNoRelation(Long attrgroupId, Map<String, Object> params);

    List<AttrGroupAndAttrVo> getGroupAndAttr(Long catelogId);
}

