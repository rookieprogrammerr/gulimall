package com.zc.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.zc.common.constant.ProductConstant;
import com.zc.common.exception.BizCodeEnum;
import com.zc.common.to.SkuHasStockVO;
import com.zc.common.to.SkuReductionTo;
import com.zc.common.to.SpuBoundsTo;
import com.zc.common.to.es.Attrs;
import com.zc.common.to.es.SkuEsModel;
import com.zc.common.utils.PageUtils;
import com.zc.common.utils.Query;
import com.zc.common.utils.R;
import com.zc.gulimall.product.dao.SpuInfoDao;
import com.zc.gulimall.product.dao.SpuInfoDescDao;
import com.zc.gulimall.product.entity.*;
import com.zc.gulimall.product.entity.vo.*;
import com.zc.gulimall.product.feign.CouponFeignService;
import com.zc.gulimall.product.feign.SearchFeignService;
import com.zc.gulimall.product.feign.WareFeignService;
import com.zc.gulimall.product.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@Service("spuInfoService")
@Slf4j
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Resource
    private SpuInfoDescDao spuInfoDescDao;

    @Resource
    private SpuImagesService spuImagesService;

    @Resource
    private ProductAttrValueService productAttrValueService;

    @Resource
    private AttrService attrService;

    @Resource
    private SkuInfoService skuInfoService;

    @Resource
    private SkuImagesService skuImagesService;

    @Resource
    private SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    private CouponFeignService couponFeignService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private WareFeignService wareFeignService;

    @Autowired
    private SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 保存spuInfo信息
     * //TODO 高级部分在完善
     * @param spuSaveVo
     */
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo spuSaveVo) {
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(spuSaveVo, spuInfoEntity);
        // 一、保存基础spu信息、pms_spu_info
        baseMapper.insert(spuInfoEntity);

        // 二、保存spu描述图片、pms_spu_info_desc
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        List<String> decripts = spuSaveVo.getDecript();
        spuInfoDescEntity.setDecript(String.join(",", decripts));
        spuInfoDescDao.insert(spuInfoDescEntity);

        // 三、保存spu图片集、pms_spu_images
        List<String> images = spuSaveVo.getImages();
        if (images != null && images.size() > 0) {
            List<SpuImagesEntity> imagesEntityList = images.stream().map(img -> {
                SpuImagesEntity spuImagesEntity = new SpuImagesEntity();
                spuImagesEntity.setSpuId(spuInfoEntity.getId());
                spuImagesEntity.setImgUrl(img);
                return spuImagesEntity;
            }).filter(entity->{
                //返回true就是需要，返回false就是剔除
                return StringUtils.isNotEmpty(entity.getImgUrl());
            }).collect(Collectors.toList());
            spuImagesService.saveBatch(imagesEntityList);
        }

        // 四、保存spu的规格参数、pms_product_attr_value
        List<BaseAttrs> baseAttrs = spuSaveVo.getBaseAttrs();
        if (baseAttrs != null && baseAttrs.size() > 0) {
            List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(baseAttr -> {
                ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
                productAttrValueEntity.setAttrId(baseAttr.getAttrId());
                productAttrValueEntity.setAttrValue(baseAttr.getAttrValues());
                productAttrValueEntity.setQuickShow(baseAttr.getShowDesc());
                AttrEntity attrEntity = attrService.getById(baseAttr.getAttrId());
                productAttrValueEntity.setAttrName(attrEntity.getAttrName());
                productAttrValueEntity.setSpuId(spuInfoEntity.getId());
                return productAttrValueEntity;
            }).collect(Collectors.toList());
            productAttrValueService.saveBatch(productAttrValueEntities);
        }

        // 五、保存spu的积分信息 gulimall_sms sms_spu_bounds
        Bounds bounds = spuSaveVo.getBounds();
        SpuBoundsTo spuBoundsTo = new SpuBoundsTo();
        BeanUtils.copyProperties(bounds, spuBoundsTo);
        spuBoundsTo.setSpuId(spuInfoEntity.getId());
        R result1 = couponFeignService.saveSpuBounds(spuBoundsTo);
        if (result1.getCode() != ProductConstant.AttrEnum.SUCCESS_FEIGN.getCode()) {
            log.error("保存spu积分信息失败");
        }

        // 六、保存spu的所有sku信息
        List<Skus> skus = spuSaveVo.getSkus();
        if (skus != null && skus.size() > 0) {
            // 获取默认图片路径
            skus.stream().forEach(sku -> {
                String default_img = "";
                for (Images image : sku.getImages()) {
                    if (image.getDefaultImg() == ProductConstant.AttrEnum.DEFAULT_IMG.getCode()) {
                        default_img = image.getImgUrl();
                    }
                }
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatelogId(spuInfoEntity.getCatelogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setSkuDefaultImg(default_img);

                // 1. sku基本信息 pms_sku_info
                skuInfoService.save(skuInfoEntity);
                // skuId
                Long skuId = skuInfoEntity.getSkuId();
                List<SkuImagesEntity> imagesEntityList = sku.getImages().stream().map(img -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setDefaultImg(img.getDefaultImg());
                    skuImagesEntity.setImgUrl(img.getImgUrl());
                    return skuImagesEntity;
                }).filter(item -> {
                    // 只保存有图片路径的
                    String imgUrl = item.getImgUrl();
                    return (!StringUtils.isEmpty(imgUrl));
                }).collect(Collectors.toList());

                // 2. sku的图片信息 pms_sku_images
                skuImagesService.saveBatch(imagesEntityList);
                List<Attr> attr = sku.getAttr();
                List<SkuSaleAttrValueEntity> skuSaleAttrValueEntityList = attr.stream().map(a -> {
                    SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                    BeanUtils.copyProperties(a, skuSaleAttrValueEntity);
                    skuSaleAttrValueEntity.setSkuId(skuId);
                    return skuSaleAttrValueEntity;
                }).collect(Collectors.toList());
                // 3. sku的属性信息 pms_sku_sale_attr_value
                skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntityList);

                // 4. sku的优惠满减信息 gulimall_sms  sms_sku_full_reduction / sms_sku_ladder / sms_member_price
                SkuReductionTo skuReductionTo = new SkuReductionTo();
                BeanUtils.copyProperties(sku, skuReductionTo);
                skuReductionTo.setSkuId(skuId);

                if (skuReductionTo.getFullCount() > 0 || skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) == 1) {
                    R result2 = couponFeignService.saveSkuReduction(skuReductionTo);
                    if (result2.getCode() != ProductConstant.AttrEnum.SUCCESS_FEIGN.getCode()) {
                        log.error("远程的sku优惠满减信息保存失败");
                    }
                }
            });
        }
    }

    /**
     * 分页查询带条件
     *
     * @param params
     * @return
     */
    @Override
    public PageUtils queryByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            wrapper.and((w) -> {
                w.eq("id", key).like("spu_name", key).or().like("spu_description", key);
            });
        }
        String catelogId = (String) params.get("catelogId");
        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq("catelog_id", catelogId);
        }
        String brandId = (String) params.get("brandId");
        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }
        String status = (String) params.get("status");
        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("publish_status", status);
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    public void up(Long spuId) {

        //1、查出当前spuid对应的所有sku信息，品牌的名字
        List<SkuInfoEntity> skus = skuInfoService.getSkuBySpuId(spuId);
        List<Long> skuIdList = skus.stream().map(SkuInfoEntity::getSkuId).collect(Collectors.toList());

        //4、查询当前sku的所有可检索的规格属性
        List<ProductAttrValueEntity> baseAttrs = productAttrValueService.baseAttrListforspu(spuId);
        //  4.1、收集attrid的集合
        List<Long> attrIds = baseAttrs.stream().map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());
        //  所有拥有检索属性的attrId
        List<Long> searchAttrIds = attrService.selectSearchAttrs(attrIds);

        Set<Long> idSet = new HashSet<>(searchAttrIds);

        List<Attrs> attrs = baseAttrs.stream().filter(item -> {
            return idSet.contains(item.getAttrId());
        }).map(item->{
            Attrs attr = new Attrs();
            BeanUtils.copyProperties(item, attr);
            return attr;
        }).collect(Collectors.toList());

        //1、发送远程调用，库存系统查询是否有库存
        Map<Long, Boolean> stockMap = null;
        try {
            //R<List<SkuHasStockVO>> skuHasStock = wareFeignService.getSkuHasStock(skuIdList);
            R result = wareFeignService.getSkuHasStock(skuIdList);

            TypeReference<List<SkuHasStockVO>> typeReference = new TypeReference<List<SkuHasStockVO>>() {};

            stockMap = result.getData(typeReference).stream().collect(Collectors.toMap(SkuHasStockVO::getSkuId, SkuHasStockVO::getHasStock));
        } catch (Exception e) {
            log.error("库存服务查询异常：原因{}", e);
        }


        //2、封装每个sku信息
        Map<Long, Boolean> finalStockMap = stockMap;
        List<SkuEsModel> upProducts = skus.stream().map(sku -> {
            //组装需要的数据
            SkuEsModel esModel = new SkuEsModel();
            BeanUtils.copyProperties(sku, esModel);

            //skuPrice、skuImg
            esModel.setSkuPrice(sku.getPrice());
            esModel.setSkuImg(sku.getSkuDefaultImg());

            //hasStack
            //设置库存信息
            if(finalStockMap.isEmpty()) {
                esModel.setHasStock(true);
            } else {
                esModel.setHasStock(finalStockMap.get(sku.getSkuId()));
            }

            //hotScope
            //TODO 2、热度评分
            esModel.setHotScore(0L);

            //brandName、brandImg
            //TODO 3、查询品牌和分类的名字信息
            BrandEntity brand = brandService.getById(esModel.getBrandId());
            esModel.setBrandName(brand.getName());
            esModel.setBrandImg(brand.getLogo());
            //catelogName
            CategoryEntity category = categoryService.getById(esModel.getCatelogId());
            esModel.setCatelogName(category.getName());

            //设置检索属性
            esModel.setAttrs(attrs);

            //attrId、attrName、attrValue
            return esModel;
        }).collect(Collectors.toList());

        //TODO 5、将数据发送给es进行保存：gulimall-search
        R r = searchFeignService.productStatusUp(upProducts);
        if(r.getCode() == 0) {
            //远程调用成功
            //TODO 6、修改当前spu的状态
            baseMapper.updateSpuStatus(spuId, ProductConstant.StatusEnum.SPU_UP.getCode());
        } else {
            //远程调用失败
            //TODO 7、重复调用？接口幂等性：重试机制？xxx
            //feign调用流程
            /**
             * 1、构造请求数据，将对象转换为json：
             *      RequestTemplate template = buildTemplateFromArgs.create(argv);
             * 2、发送请求进行执行（执行成功会解码响应数据）：
             *      executeAndDecode(template);
             * 3、执行请求会有重试机制
             *      while(true) {
             *          try {
             *              executeAndDecode(template);
             *          } catch() {
             *              try {
             *                  retryer.continueOrPropagate(e);
             *              } catch() {
             *                  throw ex;
             *              }
             *              continue;
             *          }
             *      }
             */
        }
    }

    /**
     * 根据skuId查询spu相关信息
     * @param skuId
     * @return
     */
    @Override
    public SpuInfoEntity getSpuInfoBySkuId(Long skuId) {
        SkuInfoEntity byId = skuInfoService.getById(skuId);
        return getById(byId.getSpuId());
    }

}