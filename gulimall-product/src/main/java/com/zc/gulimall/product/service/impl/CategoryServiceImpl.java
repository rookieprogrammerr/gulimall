package com.zc.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.zc.common.exception.RRException;
import com.zc.common.utils.PageUtils;
import com.zc.common.utils.Query;
import com.zc.gulimall.product.dao.CategoryDao;
import com.zc.gulimall.product.entity.CategoryEntity;
import com.zc.gulimall.product.entity.vo.Catelog2Vo;
import com.zc.gulimall.product.entity.vo.Catelog3Vo;
import com.zc.gulimall.product.service.CategoryBrandRelationService;
import com.zc.gulimall.product.service.CategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.mockito.stubbing.ValidableAnswer;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    @Autowired
    private CategoryBrandRelationService categoryBrandRelationService;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RedissonClient redisson;

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

    /**
     * 级联更新所有关联的数据
     * @CacheEvict:失效模式
     * 1、同时进行多种缓存操作 @Caching
     * 2、指定删除某个分区下的所有数据 @CacheEvict(value = "category", allEntries = true)
     * 3、存储同一类型的数据，都可以指定成同一个分区。分区名默认就是缓存的前缀
     * @param category
     */
//    @Caching(evict = {
//            @CacheEvict(value = "category", key = "'getLevel1Categorys'"),
//            @CacheEvict(value = "category", key = "'getCatelogJson'")
//    })
    @CacheEvict(value = "category", allEntries = true)  //删除分区所有的数据，失效模式
    @CachePut   //双写模式(方法需要返回值)
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }


    /**
     * 1、每一个需要缓存的数据我们都来指定要放到哪个名字的缓存。【缓存的分区(按照业务类型来区分)】
     * 2、@Cacheable({"category"})
     *      代表当前方法的结果需要缓存，如果缓存中有，方法不用调用。
     *      如果缓存中没有，会调用方法，最后将方法的结果放入缓存。
     * 3、默认行为
     *      1）、如果缓存中有，方法不用调用。
     *      2）、key是默认自动生成：缓存的名字::SimpleKey []（自主生成的key值）
     *      3）、缓存的value的值。默认使用jdk序列化机制，将序列化后的数据存入redis
     *      4）、默认ttl的时间是-1；
     *
     *    自定义：
     *      1）、指定生成的缓存使用的key    key属性指定，接收一个SpEl
     *      2）、指定缓存的数据的存活时间    配置文件中修改ttl
     *      3）、将数据保存为json格式
     *              CacheAutoConfiguration
     *              RedisCacheConfiguration
     *  4、Spring-Cache的不足：
     *      1）、读模式：
     *          缓存穿透：查询一个null数据。解决方案：缓存空数据（cache-null-values: true）
     *          缓存击穿：大量并发进来同时查询一个刚好过期的数据。解决方案：加锁：默认是无锁的
     *          缓存雪崩：大量的key同时过期。解决方案：加随机时间。加上过期时间（time-to-live: 360000）
     *      2）、写模式：（缓存与数据库一致）
     *          1）、读写加锁。
     *          2）、引入Canal，感知到MySQL的更新去更新数据库
     *          3）、读多写多，直接去数据库查询就行
     *      总结：
     *          常规数据（读多写少。即时性，一致性不高的数据）：完全可以使用Spring-Cache：写模式（只要缓存的数据有过期时间就足够了）
     *
     *          特殊数据：特殊设计
     *      原理：
     *          CacheManager(RedisCacheManager)->Cache(RedisCache)->Cache负责缓存的读写
     *
     * @return
     */
    @Cacheable(value = {"category"}, key = "#root.method.name", sync = true)    //开启同步后执行get()，不开启执行lookup()
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        long l = System.currentTimeMillis();
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        System.out.println("消耗时间：" + (System.currentTimeMillis() - l));
        return categoryEntities;
    }

    @Cacheable(value = "category", key = "#root.methodName")
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        System.out.println("查询了数据库.....");

        List<CategoryEntity> selectList = baseMapper.selectList(null);
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



    //TODO 产生堆外内存溢出：OutOfDirectMemoryError
    //  1、springboot2.0以后默认使用lettuce作为操作redis的客户端。它使用netty进行网络通信。
    //  2、lettuce的bug导致堆外内存溢出   -Xmx300m；netty如果没有执行堆外内存默认使用-Xmx300m
    //      可以通过-Dio.netty.maxDirectMemory设置
    //  解决方案：不能使用-Dio.netty.maxDirectMemory只去调大堆外内存
    //  1、升级lettuce客户端  2、切换使用jedis
    public Map<String, List<Catelog2Vo>> getCatelogJson2() {
        //  给缓存中放json字符串，拿出的json字符串，还用逆转为能用的对象类型；【序列化与反序列化】

        /**
         * 1、空结果缓存：解决缓存穿透
         * 2、设置过期时间（加随机值）：解决缓存雪崩
         *
         */

        //  1、加入缓存逻辑，缓存中存的数据是json字符串
        //  JSON好处：跨语言跨平台兼容
        String catelogJSON = redisTemplate.opsForValue().get("catelogJSON");
        if (StringUtils.isEmpty(catelogJSON)) {
            //  2、缓存中没有，查询数据库
            System.out.println("缓存不命中....将要查询数据库...");
            Map<String, List<Catelog2Vo>> catelogJsonFromDB = getCatelogJsonFromDBWithRedisLock();

            return catelogJsonFromDB;
        }

        System.out.println("缓存命中....直接返回....");
        //  转为我们指定的对象
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catelogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });

        return result;
    }


    /**
     * 利用redisson实现分布式锁
     * 问题：
     *  缓存里面的数据如何和数据库保持一致？
     *      缓存数据一致性
     *          1)、双写模式
     *          2)、失效模式
     *
     * @return
     */
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDBWithRedissonLock() {

        //  1、锁的名字。锁的粒度，越细越快。
        //锁的粒度：具体缓存的是某个数据，11-号商品（product-11-lock）12-号商品（product-12-lock）
        RLock lock = redisson.getLock("CatelogJson-lock");
        lock.lock();

        Map<String, List<Catelog2Vo>> dataFromDB;
        try {
            dataFromDB = getDataFromDB();
        } finally {
            //删除锁（原子删锁）
            lock.unlock();
        }

        return dataFromDB;

    }


    /**
     * 利用redis实现分布式锁
     *
     * @return
     */
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDBWithRedisLock() {

        //  1、占分布式锁。去redis占坑
        String uuid = UUID.randomUUID().toString();
        Boolean lock = redisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("获取分布式锁成功...");
            //  加锁成功...执行业务
            //  2、设置过期时间，必须和加锁是同步的，原子的
            //redisTemplate.expire("lock", 30, TimeUnit.SECONDS);
            Map<String, List<Catelog2Vo>> dataFromDB;
            try {
                dataFromDB = getDataFromDB();
            } finally {
                //  获取值对比+对比成功删除=原子操作   Lua脚本解锁
                String script = "if redis call('get',KEYS[1]) == ARGV[1] then return redis call('del',KEYS[1]) else return 0 end";
                //删除锁（原子删锁）
                try {
                    Thread.sleep(200);
                } catch (Exception e) {
                }

                Long lock1 = redisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            }


//            String lockValue = redisTemplate.opsForValue().get("lock");
//            if(uuid.equals(lockValue)) {
//                //删除自己的锁
//                redisTemplate.delete("lock");   //删除锁
//            }

            return dataFromDB;
        } else {
            //  加锁失败...重试
            //  休眠100ms重试
            System.out.println("获取分布式锁失败...等待重试");
            return getCatelogJsonFromDBWithRedisLock(); //自旋的方式
        }

    }

    /**
     * 判断：如果缓存重有则直接返回，没有则去查询数据库
     * 返回三级分类信息
     *
     * @return
     */
    private Map<String, List<Catelog2Vo>> getDataFromDB() {
        String catelogJSON = redisTemplate.opsForValue().get("catelogJSON");
        if (StringUtils.isNotEmpty(catelogJSON)) {
            //缓存不为空直接返回
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catelogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return result;
        }
        System.out.println("查询了数据库.....");

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

        //  3、查到的数据放入缓存，将对象转为json
        String toJSONString = JSON.toJSONString(parent_cid);
        redisTemplate.opsForValue().set("catelogJSON", toJSONString, 1, TimeUnit.DAYS);
        return parent_cid;
    }

    /**
     * 从数据库查询并封装分类数据
     *
     * @return
     */
    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDBWithLocalLock() {
        //  只要是同一把锁，就能锁住需要这个锁的所有线程
        //  1、synchronized (this) ：Springboot所有的组件在容器中都是单例的。
        //  TODO 本地锁：synchronized，JUC（Lock），在分布式情况下，想要锁住所有，必须使用分布式锁
        synchronized (this) {
            //  得到锁以后，我们应该再去缓存中确定一次，如果没有才需要继续查询
            String catelogJSON = redisTemplate.opsForValue().get("catelogJSON");
            if (StringUtils.isNotEmpty(catelogJSON)) {
                //缓存不为空直接返回
                Map<String, List<Catelog2Vo>> result = JSON.parseObject(catelogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
                });
                return result;
            }
            System.out.println("查询了数据库.....");
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

            //  3、查到的数据放入缓存，将对象转为json
            String toJSONString = JSON.toJSONString(parent_cid);
            redisTemplate.opsForValue().set("catelogJSON", toJSONString, 1, TimeUnit.DAYS);
            return parent_cid;
        }
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