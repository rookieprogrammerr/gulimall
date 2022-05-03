package com.zc.gulimall.product.web;

import com.zc.gulimall.product.entity.CategoryEntity;
import com.zc.gulimall.product.entity.vo.Catelog2Vo;
import com.zc.gulimall.product.service.CategoryService;
import org.redisson.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Controller
public class IndexController {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private RedissonClient redisson;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {

        //TODO 1、查出所有的1级分类
        List<CategoryEntity> categoryEntityList = categoryService.getLevel1Categorys();

        model.addAttribute("categorys", categoryEntityList);
        return "index";
    }

    @ResponseBody
    @GetMapping("/index/catelog.json")
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        Map<String, List<Catelog2Vo>> map = categoryService.getCatelogJson();
        return map;
    }


    @ResponseBody
    @GetMapping("/hello")
    public String hello() {
        //  1、获取一把锁，只要锁的名字一样，就是同一把锁
        RLock lock = redisson.getLock("my-lock");

        //  2、加锁
        lock.lock(); //阻塞式等待。默认加的锁都是30s时间。
        //  1)、锁的自动续期，如果业务超长，运行期间自动给锁续上新的30s。不用担心业务时间长，锁自动过期被删掉
        //  2)、加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认在30s以后自动删除。

//        lock.lock(10, TimeUnit.SECONDS); //10秒后自动解锁，自动解锁时间一定要大于业务的执行时间
        //问题：lock.lock(10, TimeUnit.SECONDS); 在锁时间到了以后，不会自动续期
        //  1、如果我们传递了锁的超时时间，就发送给redis执行脚本，进行占锁，默认超时就是我们指定的时间
        //  2、如果我们未指定锁的超时时间，就使用30 * 1000【LockWatchdogTimeout看门狗的默认时间】；
        //  只要占锁成功，就会启动一个定时任务【重新给锁设置过期时间，新的过期时间就是看门的默认时间】，每隔10s都会自动再次续期，续成30s
        //  internalLockLeaseTime【看门狗时间】 / 3，10s

        //  最佳实战
        //  1)、lock.lock(30, TimeUnit.SECONDS);省掉了整个续期操作。手动解锁
        try {
            System.out.println("加锁成功，执行业务..." + Thread.currentThread().getId());
            Thread.sleep(30000);
        } catch (Exception e) {

        } finally {
            //  3、解锁    假设解锁代码没有运行，redisson会不会出现死锁
            System.out.println("释放锁..." + Thread.currentThread().getId());
            lock.unlock();
        }

        return "hello";
    }


    @ResponseBody
    @GetMapping("/write")
    public String writeValue() {

        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        String s = "";
        RLock rLock = lock.writeLock();
        try {
            //  1、改数据加写锁，读数据加读锁
            rLock.lock();
            System.out.println("写锁加锁成功..." + Thread.currentThread().getId());
            s = UUID.randomUUID().toString();
            Thread.sleep(30000);
            redisTemplate.opsForValue().set("writeValue", s);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
            System.out.println("写锁释放..." + Thread.currentThread().getId());
        }

        return s;
    }


    //保证一定能读到最新数据，修改期间，写锁是一个排他锁（互斥锁、独享锁）。读锁是一个共享锁
    //写锁没释放，读锁就必须等待
    //  读 + 读：相当于无锁，并发读，只会在redis中记录好，所有当前的读锁。他们都会同时加锁成功
    //  写 + 读，等待写锁释放
    //  写 + 写，阻塞方式
    //  读 + 写：有读锁，写也需要等待
    //  总结：只要有写的存在，都必须等待
    @ResponseBody
    @GetMapping("/read")
    public String readValue() {
        RReadWriteLock lock = redisson.getReadWriteLock("rw-lock");
        String s = "";
        //  加读锁
        RLock rLock = lock.readLock();
        rLock.lock();
        try {
            System.out.println("读锁加锁成功..." + Thread.currentThread().getId());
            s = redisTemplate.opsForValue().get("writeValue");
            Thread.sleep(30000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            rLock.unlock();
            System.out.println("读锁释放..." + Thread.currentThread().getId());
        }

        return s;
    }


    /**
     * 车库停车
     * 3车位
     * 信号量也可以用作分布式限流
     */
    @GetMapping("/park")
    @ResponseBody
    public String park() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
//        park.acquire();//获取一个信号，获取一个值（占一个车位）
        boolean b = park.tryAcquire();
        if(b) {
            //执行业务
        } else {
            return "error";
        }
        return "ok=>" + b;
    }

    @GetMapping("/go")
    @ResponseBody
    public String go() throws InterruptedException {
        RSemaphore park = redisson.getSemaphore("park");
        park.release();//释放一个信号，（释放一个车位）

        return "ok";
    }


    /**
     * 放假、锁门
     * 5个班全部走完，才可以锁门
     */
    @GetMapping("/lockDoor")
    @ResponseBody
    public String lockDoor() throws InterruptedException {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.trySetCount(5);
        door.await();   //等待闭锁都完成

        return "放假了...";

    }

    @GetMapping("/gogogo/{id}")
    @ResponseBody
    public String gogogo(@PathVariable("id") Long id) {
        RCountDownLatch door = redisson.getCountDownLatch("door");
        door.countDown();//计数器-1

        return id + "班的人都走了...";

    }
}
