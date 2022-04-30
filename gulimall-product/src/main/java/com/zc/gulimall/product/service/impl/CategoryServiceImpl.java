package com.zc.gulimall.product.service.impl;

import com.zc.common.exception.RRException;
import com.zc.common.utils.PageUtils;
import com.zc.common.utils.Query;
import com.zc.gulimall.product.dao.CategoryDao;
import com.zc.gulimall.product.entity.CategoryEntity;
import com.zc.gulimall.product.entity.vo.Catelog2Vo;
import com.zc.gulimall.product.entity.vo.Catelog3Vo;
import com.zc.gulimall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {


    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 树型显示
     *
     * @return
     */
    @Override
    public List<CategoryEntity> listWithTree() {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);

        List<CategoryEntity> treeMenus = categoryEntities.stream().filter((categoryEntity) -> {
            return categoryEntity.getParentCid() == 0;
        }).sorted((menu1, menu2) -> {
            return (menu1.getSort() == null ? 0 : menu1.getSort()) - (menu2.getSort() == null ? 0 : menu2.getSort());
        }).map((menu) -> {
            menu.setChildren(getChildren(menu, categoryEntities));
            return menu;
        }).collect(Collectors.toList());

        return treeMenus;
    }


    private List<CategoryEntity> getChildren(CategoryEntity rootMenu, List<CategoryEntity> allMenus) {

        List<CategoryEntity> childrenList = allMenus.stream().filter(categoryEntity -> {
            return categoryEntity.getParentCid().equals(rootMenu.getCatId());
        }).map(menu -> {
            menu.setChildren(getChildren(menu, allMenus));
            return menu;
        }).sorted((m1, m2) -> {
            return (m1.getSort() == null ? 0 : m1.getSort()) - (m2.getSort() == null ? 0 : m2.getSort());
        }).collect(Collectors.toList());
        return childrenList;
    }

    @Override
    public void removeMenus(List<Long> asList) {
        System.out.println("删除的数据为： ++ ++++" + asList);
        // 逻辑删除
        int result = baseMapper.deleteBatchIds(asList);
        if (result < 0) {
            throw new RRException("删除失败");
        }
    }

    /**
     * 寻找一个category的全路径  example [2.3,6]
     *
     * @param catelogId
     * @return
     */
    @Override
    public Long[] findCategoryPath(Long catelogId) {
        List<Long> path = new ArrayList<>();
        List<Long> parentPath = findParentPath(catelogId, path);
        Collections.reverse(parentPath);
        return parentPath.toArray(new Long[parentPath.size()]);
    }

    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        long l = System.currentTimeMillis();
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        System.out.println("消耗时间：" + (System.currentTimeMillis() - l));
        return categoryEntities;
    }

    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        /**
         * 优化：1、将数据库的多次查询变为一次
         */

        List<CategoryEntity> selectList = baseMapper.selectList(null);


        /**
         * 逻辑
         */
        //  1、查出所有一级分类
        List<CategoryEntity> level1Categorys = getParentCid(selectList, 0L);

        //  2、封装结果集

        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream().collect(Collectors.toMap(k -> {
            //  key
            return k.getCatId().toString();
        }, v -> {
            //  value
            //  1、每一个的一级分类，查到这个一级分类的二级分类
            List<CategoryEntity> categoryEntities = getParentCid(selectList, v.getCatId());
            //  2、封装上面的结果
            List<Catelog2Vo> catelog2Vos = null;
            if (categoryEntities != null) {
                catelog2Vos = categoryEntities.stream().map(lv2 -> {
                    Catelog2Vo catelog2Vo = new Catelog2Vo(v.getCatId().toString(), null, lv2.getCatId().toString(), lv2.getName());
                    //  1、找当前二级分类的三级分类封装成vo
                    List<CategoryEntity> level3Catelog = getParentCid(selectList, lv2.getCatId());

                    List<Catelog3Vo> catelog3Vos = null;
                    if (level3Catelog != null) {
                        //  2、封装成指定格式
                        catelog3Vos = level3Catelog.stream().map(lv3 -> {
                            Catelog3Vo catelog3Vo = new Catelog3Vo(lv2.getCatId().toString(), lv3.getCatId().toString(), lv3.getName());

                            return catelog3Vo;
                        }).collect(Collectors.toList());
                        catelog2Vo.setCatelog3List(catelog3Vos);
                    }

                    return catelog2Vo;
                }).collect(Collectors.toList());
            }
            return catelog2Vos;
        }));

        return parent_cid;
    }

    private List<CategoryEntity> getParentCid(List<CategoryEntity> selectList, Long parentCid) {
        List<CategoryEntity> collect = selectList.stream().filter(item -> item.getParentCid() == parentCid).collect(Collectors.toList());
        //return baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", v.getCatId()));
        return collect;
    }

    private List<Long> findParentPath(Long catelogId, List<Long> path) {
        //收集当前节点id
        path.add(catelogId);
        CategoryEntity categoryEntity = baseMapper.selectById(catelogId);
        if (categoryEntity.getParentCid() != 0) {
            findParentPath(categoryEntity.getParentCid(), path);
        }
        return path;
    }
}