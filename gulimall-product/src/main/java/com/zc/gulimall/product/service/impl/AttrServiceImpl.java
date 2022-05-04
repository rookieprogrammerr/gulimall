package com.zc.gulimall.product.service.impl;

import com.zc.common.constant.ProductConstant;
import com.zc.common.utils.PageUtils;
import com.zc.common.utils.Query;
import com.zc.gulimall.product.dao.*;
import com.zc.gulimall.product.entity.*;
import com.zc.gulimall.product.entity.vo.AttrResVo;
import com.zc.gulimall.product.entity.vo.AttrVo;
import com.zc.gulimall.product.service.AttrService;
import com.zc.gulimall.product.service.ProductAttrValueService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Resource
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Resource
    private AttrGroupDao attrGroupDao;

    @Resource
    private CategoryDao categoryDao;

    @Resource
    private CategoryServiceImpl categoryService;

    @Resource
    private ProductAttrValueService productAttrValueService;

    @Resource
    private ProductAttrValueDao productAttrValueDao;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public PageUtils queryByCid(Map<String, Object> params, Long catelogId, String attrType) {
        QueryWrapper<AttrEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((obj) -> {
                obj.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        PageUtils pageUtils = null;
        IPage<AttrEntity> page = null;
        if (catelogId != 0) {
            wrapper.eq("catelog_id", catelogId);
        }
        wrapper.eq("attr_type", "base".equalsIgnoreCase(attrType) ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        page = this.page(new Query<AttrEntity>().getPage(params),
                wrapper);
        pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();
        // 需要将groupName 和 catelogName 一同返回
        List<AttrResVo> attrResVoList = records.stream().map((attrEntity) -> {
            AttrResVo attrResVo = new AttrResVo();
            BeanUtils.copyProperties(attrEntity, attrResVo);
            AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationDao
                    .selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>()
                            .eq("attr_id", attrEntity.getAttrId()));
            if ("base".equalsIgnoreCase(attrType)) {
                if (relationEntity != null && relationEntity.getAttrGroupId() != null) {
                    AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());
                    attrResVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
            CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
            if (categoryEntity != null) {
                attrResVo.setCatelogName(categoryEntity.getName());
            }
            return attrResVo;
        }).collect(Collectors.toList());
        pageUtils.setList(attrResVoList);
        return pageUtils;
    }

    @Transactional
    @Override
    public void saveVo(AttrVo attr) {
        AttrEntity attrEntity = new AttrEntity();
        if (attrEntity != null) {
            BeanUtils.copyProperties(attr, attrEntity);
        }
        baseMapper.insert(attrEntity);
        // 保存分组关系
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attr.getAttrGroupId() != null) {
            AttrAttrgroupRelationEntity groupEntity = new AttrAttrgroupRelationEntity();
            groupEntity.setAttrGroupId(attr.getAttrGroupId());
            groupEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationDao.insert(groupEntity);
        }
    }

    @Override
    public AttrResVo getAttrInfo(Long attrId) {
        AttrEntity attrEntity = baseMapper.selectById(attrId);
        AttrResVo attrResVo = new AttrResVo();
        BeanUtils.copyProperties(attrEntity, attrResVo);
        AttrAttrgroupRelationEntity relationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>()
                .eq("attr_id", attrEntity.getAttrId()));
        // 将attrGroupId 和 GroupName 返回
        if (relationEntity != null) {
            attrResVo.setAttrGroupId(relationEntity.getAttrGroupId());
            AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(relationEntity.getAttrGroupId());
            if (attrGroupEntity != null) {
                attrResVo.setGroupName(attrGroupEntity.getAttrGroupName());
            }
        }
        // 将catelogPath和 catlogName 返回
        Long[] categoryPath = categoryService.findCatelogPath(attrEntity.getCatelogId());
        attrResVo.setCatelogPath(categoryPath);
        CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
        if (categoryEntity != null) {
            attrResVo.setCatelogName(categoryEntity.getName());
        }
        return attrResVo;
    }

    @Transactional
    @Override
    public void updateVo(AttrVo attr) {
        // 修改基本属性
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attr, attrEntity);
        baseMapper.updateById(attrEntity);
        // 修改分组关联
        if (attr.getAttrType() == ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()) {
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attr.getAttrGroupId());
            Integer count = attrAttrgroupRelationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>()
                    .eq("attr_id", attr.getAttrId()));
            if (count > 0) {
                attrAttrgroupRelationDao.update(relationEntity, new QueryWrapper<AttrAttrgroupRelationEntity>()
                        .eq("attr_id", attrEntity.getAttrId()));
            } else {
                relationEntity.setAttrId(attr.getAttrId());
                attrAttrgroupRelationDao.insert(relationEntity);
            }
        }
    }

    /**
     * 获取spu规格
     *
     * @param spuId
     * @return
     */
    @Override
    public List<ProductAttrValueEntity> getSpuSpecification(Long spuId) {
        List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueDao.selectList(new QueryWrapper<ProductAttrValueEntity>()
                .eq("spu_id", spuId));
        return productAttrValueEntities;
    }

    /**
     * 修改商品规格
     *
     * @param spuId
     * @param productAttrValueEntities
     */
    @Transactional
    @Override
    public void updateSpecification(Long spuId, List<ProductAttrValueEntity> productAttrValueEntities) {
        // 删除老数据
        productAttrValueService.remove(new QueryWrapper<ProductAttrValueEntity>()
                .eq("spu_id", spuId));
        // 插入新数据
        List<ProductAttrValueEntity> collect = productAttrValueEntities.stream().map(item -> {
            item.setSpuId(spuId);
            return item;
        }).collect(Collectors.toList());
        productAttrValueService.saveBatch(collect);
    }

    /**
     * 在指定的所有属性集合里面，挑出检索属性
     * @param attrIds
     * @return
     */
    @Override
    public List<Long> selectSearchAttrs(List<Long> attrIds) {
        /**
         * select attr_id from `pms_attr` where attr_id in (?) and search_type=1
         */
        return baseMapper.selectSearchAttrIds(attrIds);
    }
}