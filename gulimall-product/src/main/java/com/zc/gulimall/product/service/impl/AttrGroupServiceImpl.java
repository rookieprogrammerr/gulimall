package com.zc.gulimall.product.service.impl;

import com.zc.common.constant.ProductConstant;
import com.zc.common.utils.PageUtils;
import com.zc.common.utils.Query;
import com.zc.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.zc.gulimall.product.dao.AttrDao;
import com.zc.gulimall.product.dao.AttrGroupDao;
import com.zc.gulimall.product.dao.CategoryDao;
import com.zc.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.zc.gulimall.product.entity.AttrEntity;
import com.zc.gulimall.product.entity.AttrGroupEntity;
import com.zc.gulimall.product.entity.CategoryEntity;
import com.zc.gulimall.product.entity.vo.AttrGroupAndAttrVo;
import com.zc.gulimall.product.entity.vo.AttrGroupRelationVo;
import com.zc.gulimall.product.entity.vo.SkuItemVo;
import com.zc.gulimall.product.entity.vo.SpuItemBaseAttrGroupVo;
import com.zc.gulimall.product.service.AttrGroupService;
import com.zc.gulimall.product.service.AttrService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrGroupService")
public class AttrGroupServiceImpl extends ServiceImpl<AttrGroupDao, AttrGroupEntity> implements AttrGroupService {

    @Resource
    private AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Resource
    private AttrDao attrDao;

    @Resource
    private CategoryDao categoryDao;

    @Resource
    private AttrService attrService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrGroupEntity> page = this.page(
                new Query<AttrGroupEntity>().getPage(params),
                new QueryWrapper<AttrGroupEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 根据条件和catelogId分页查询分组信息
     *
     * @param params
     * @param catelogId
     * @return
     */
    @Override
    public PageUtils queryPageByCid(Map<String, Object> params, Long catelogId) {
        QueryWrapper<AttrGroupEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((obj) -> {
                obj.eq("attr_group_id", key).or().like("attr_group_name", key);
            });
        }
        if (catelogId == 0) {
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),
                    wrapper);
            return new PageUtils(page);
        } else {
            wrapper.eq("catelog_id", catelogId);
            IPage<AttrGroupEntity> page = this.page(new Query<AttrGroupEntity>().getPage(params),
                    wrapper);
            return new PageUtils(page);
        }
    }

    /**
     * 根据attrGroupId 查询attr
     *
     * @param attrgroupId
     * @return
     */
    @Override
    public List<AttrEntity> getAttrByAttrGroupId(Long attrgroupId) {
        List<AttrAttrgroupRelationEntity> relationEntityList = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>()
                .eq("attr_group_id", attrgroupId));
        List<Long> attrIdList = relationEntityList.stream().map((attrAttrgroupRelationEntity) -> {
            Long attrId = attrAttrgroupRelationEntity.getAttrId();
            return attrId;
        }).collect(Collectors.toList());
        List<AttrEntity> attrEntities = null;
        if (attrIdList != null && attrIdList.size() > 0) {
            attrEntities = attrDao.selectBatchIds(attrIdList);
        }
        return attrEntities;
    }

    /**
     * 根据attrId和attrGroupId删除关联
     *
     * @param attrGroupRelationVo
     */
    @Override
    public void deleteRelation(AttrGroupRelationVo[] attrGroupRelationVo) {
        List<AttrAttrgroupRelationEntity> entityList = Arrays.stream(attrGroupRelationVo).map((item) -> {
            AttrAttrgroupRelationEntity attrAttrgroupRelationEntity = new AttrAttrgroupRelationEntity();
            BeanUtils.copyProperties(item, attrAttrgroupRelationEntity);
            return attrAttrgroupRelationEntity;
        }).collect(Collectors.toList());
        attrAttrgroupRelationDao.deleteBathRelations(entityList);
    }

    /**
     * 获取当前分组未关联的属性
     *
     * @param attrgroupId
     * @param params
     * @return
     */
    @Override
    public PageUtils getNoRelation(Long attrgroupId, Map<String, Object> params) {
        // 只能是当前分类
        AttrGroupEntity attrGroupEntity = baseMapper.selectById(attrgroupId);
        CategoryEntity categoryEntity = null;
        PageUtils pageUtils = null;
        if (attrGroupEntity != null) {
            categoryEntity = categoryDao.selectById(attrGroupEntity.getCatelogId());
            // 没有被其他引用(查询当前分类下的其他分组)
            List<AttrGroupEntity> groupEntityList = baseMapper.selectList(new QueryWrapper<AttrGroupEntity>()
                    .eq("catelog_id", attrGroupEntity.getCatelogId()));
            List<Long> groupIdList = groupEntityList.stream().map((item) -> {
                return item.getAttrGroupId();
            }).collect(Collectors.toList());
            QueryWrapper<AttrAttrgroupRelationEntity> wrapper1 = new QueryWrapper<>();
            if (groupIdList != null && groupEntityList.size() > 0) {
                wrapper1.in("attr_group_id", groupIdList);
            }
            // 查询这些分组关联的属性
            List<AttrAttrgroupRelationEntity> relationEntityList = attrAttrgroupRelationDao.selectList(wrapper1);
            List<Long> attrIdList = relationEntityList.stream().map((item) -> {
                return item.getAttrId();
            }).collect(Collectors.toList());
            // 获取未被关联的属性的条件
            QueryWrapper<AttrEntity> wrapper2 = new QueryWrapper<AttrEntity>()
                    .eq("catelog_id", attrGroupEntity.getCatelogId())
                    .eq("attr_type", ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
            if (attrIdList != null && attrIdList.size() > 0) {
                wrapper2.notIn("attr_id", attrIdList);
            }
            String key = (String) params.get("key");
            if (!StringUtils.isEmpty(key)) {
                wrapper2.and((w) -> {
                    w.eq("attr_id", key).or().like("attr_name", key);
                });
            }
            IPage<AttrEntity> page = attrService.page(new Query<AttrEntity>().getPage(params), wrapper2);
            pageUtils = new PageUtils(page);
        }
        return pageUtils;
    }

    @Override
    public List<AttrGroupAndAttrVo> getGroupAndAttr(Long catelogId) {
        List<AttrGroupEntity> groupEntityList = baseMapper.selectList(new QueryWrapper<AttrGroupEntity>().eq("catelog_id", catelogId));
        List<AttrGroupAndAttrVo> attrVoList = new ArrayList<>(100);
        for (int i = 0; i < groupEntityList.size(); i++) {
            AttrGroupEntity attrGroupEntity = groupEntityList.get(i);
            List<AttrAttrgroupRelationEntity> attrAttrgroupRelationEntityList = attrAttrgroupRelationDao.selectList(new QueryWrapper<AttrAttrgroupRelationEntity>()
                    .eq("attr_group_id", attrGroupEntity.getAttrGroupId()));
            List<Long> attrIds = attrAttrgroupRelationEntityList.stream().map(item -> {
                return item.getAttrId();
            }).collect(Collectors.toList());
            List<AttrEntity> attrEntities = null;
            if (attrIds != null && attrIds.size() > 0) {
                attrEntities = attrDao.selectBatchIds(attrIds);
            }
            AttrGroupAndAttrVo attrGroupAndAttrVo = new AttrGroupAndAttrVo();
            BeanUtils.copyProperties(attrGroupEntity, attrGroupAndAttrVo);
            attrGroupAndAttrVo.setAttrs(attrEntities);
            attrVoList.add(attrGroupAndAttrVo);
        }
        return attrVoList;
    }

    /**
     * 根据spuid获取所有的属性分组及属性值
     *
     * @param spuId
     * @param catelogId
     * @return
     */
    @Override
    public List<SpuItemBaseAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catelogId) {
        //1、查出当前spu对应的所有属性的分组信息以及当前分组下的所有属性对应的值
        List<SpuItemBaseAttrGroupVo> vos = this.baseMapper.getAttrGroupWithAttrsBySpuId(spuId, catelogId);
        return vos;
    }
}