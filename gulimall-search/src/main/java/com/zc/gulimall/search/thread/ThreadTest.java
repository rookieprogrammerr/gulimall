package com.zc.gulimall.search.thread;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

@Slf4j
public class ThreadTest {
    public static ExecutorService service = Executors.newFixedThreadPool(10);

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main....start....");

//        CompletableFuture<Void> completableFuture = CompletableFuture.runAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结果：" + i);
//        }, service);


        /**
         * 方法完成后的感知
         */
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 0;
//            System.out.println("运行结果：" + i);
//
//            return i;
//        }, service).whenComplete((result, exception)-> {    //可以处理正常和异步的计算结果
//            //  虽然能得到异常信息，但是没法修改返回数据。类似于监听器
//            System.out.println("异步任务成功完成了...结果是" + result + "；异常时" + exception);
//        }).exceptionally(throwable -> { //处理异常情况
//            //  可以感知异常，同时返回默认值
//            return 10;
//        });

        /**
         * 方法执行完成后的处理
         */
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 0;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, service).handle((result, exception) -> {
//            if(result != null) {
//                return result * 2;
//            }
//            if(exception != null) {
//                return 0;
//            }
//            return 0;
//        });


        /**
         * 串行化
         * 1）、thenRun：不能获取到上一步的执行结果，无返回值
         * 2）、能接收上一步的结果，但是无返回值
         * 3）、能接受上一步的结果，有返回值
         */
        //  =================== 1 ==================
//        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, service).thenRunAsync(() -> {
//            System.out.println("任务2启动了");
//        }, service);

        //  =================== 2 ==================
//        CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, service).thenAcceptAsync(res-> {
//            System.out.println("任务2启动了" + res);
//        }, service);

        //  =================== 3 ==================
//        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
//            System.out.println("当前线程：" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("运行结果：" + i);
//            return i;
//        }, service).thenApplyAsync(res -> {
//            System.out.println("任务2启动了" + res);
//
//            return "hello" + res;
//        }, service);

        //  future.get()：阻塞

        /**
         * 两个都完成
         */
//        CompletableFuture<Object> future01 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务1线程：" + Thread.currentThread().getId());
//            int i = 10 / 2;
//            System.out.println("任务1结束：" + i);
//            return i;
//        }, service);
//
//        CompletableFuture<Object> future02 = CompletableFuture.supplyAsync(() -> {
//            System.out.println("任务2线程：" + Thread.currentThread().getId());
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//
//            System.out.println("任务2结束：");
//            return "Hello";
//        }, service);

        //runAfterBothAsync：组合两个future，不需要获取future的结果，只需两个future处理完任务后，处理该任务
//        future01.runAfterBothAsync(future02, ()-> {
//            System.out.println("任务3开始");
//        }, service);

        //thenAcceptBothAsync：组合两个future，获取两个future任务的返回结果，然后处理任务，没有返回值
//        future01.thenAcceptBothAsync(future02, (f1, f2)-> {
//            System.out.println("任务3开始...之前的结果：" + f1 + "-->" + f2);
//        }, service);

        //thenCombineAsync：组合两个future，获取两个future的返回结果，并返回当前任务的返回值
//        CompletableFuture<String> future = future01.thenCombineAsync(future02, (f1, f2) -> {
//            return f1 + ":" + f2 + "-> Hello";
//        }, service);


        /**
         * 当两个任务中，任意一个future任务完成的时候，执行任务
         */
        //applyToEither：两个任务有一个执行完成，获取它的返回值，处理任务并有新的返回值。
//        CompletableFuture<String> future = future01.applyToEitherAsync(future02, (res) -> {
//            System.out.println("任务3开始...之前的结果：" + res);
//            return res.toString() + "哈哈";
//        }, service);

        //acceptEither：两个任务有一个执行完成，获取它的返回值，处理任务，没有新的返回值
//        future01.acceptEitherAsync(future02, (res)-> {
//            System.out.println("任务3开始...之前的结果：" + res);
//        }, service);

        //runAfterEither：两个任务有一个执行完成，不需要获取future的结果，处理任务，也没有返回值。
//        future01.runAfterEitherAsync(future02, ()-> {
//            System.out.println("任务3开始");
//        }, service);


        /**
         * 多任务组合
         */
        CompletableFuture<String> futureImg = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的图片信息");
            return "hello.jpg";
        }, service);

        CompletableFuture<String> futureAttr = CompletableFuture.supplyAsync(() -> {
            System.out.println("查询商品的属性");
            return "黑色+256G";
        }, service);

        CompletableFuture<String> futureDesc = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            System.out.println("查询商品的介绍");
            return "华为";
        }, service);

        //allOf：等待所有任务完成
//        CompletableFuture<Void> allOf = CompletableFuture.allOf(futureImg, futureAttr, futureDesc);
//        allOf.get();    //等待所有结果完成

        //anyOf：只要有一个任务完成
        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(futureImg, futureAttr, futureDesc);
        System.out.println("main....end...." + anyOf.get());
    }


    public void thread(String[] args) throws ExecutionException, InterruptedException {
        System.out.println("main....start....");
        /**
         * 1）、继承Thread
         * 2）、实现Runnable接口
         * 3）、实现Callable接口 + FutureTask （可以拿到返回结果，可以处理异步）
         * 4）、线程池【ExecutorService】
         *          给线程池直接提交任务。
         *          service.execute(new Runable01());
         *        1、创建：
         *            1）、Executors
         *            2）、new ThreadPoolExector
         *       Future：可以获取到异步结果
         * 区别：
         *      1、2不能得到返回值。3可以获取返回值
         *      1、2、3都不能控制资源
         *      4可以控制资源：性能稳定。
         */


//        Thread01 thread = new Thread01();
//        thread.start();//启动线程


//        Runable01 runable01 = new Runable01();
//        new Thread(runable01).start();


//        FutureTask<Integer> futureTask = new FutureTask<>(new Callable01());
//        new Thread(futureTask).start();
//
//        //阻塞等待整个线程执行完成，获取返回结果
//        Integer integer = futureTask.get();


        //  我们以后在业务代理里面，以上3种启动线程的方式都不用。将所有的多线程异步任务都交给线程池执行
//        new Thread(() -> System.out.println("hello")).start();

        //当前系统中池只有一两个，每个异步任务，提交给线程池让他自己去执行
        service.execute(new Runable01());
        /**
         * 七大参数
         *      1、corePoolSize[5]：核心线程数【一直存在，除非设置了allowCoreThreadTimeOut】；线程池，创建好以后就准备就绪的线程数量，就等待来接收异步任务去执行
         *              5个  Thread thread = new Thread();  thread.start();
         *      2、maximumPoolSize：最大线程数量；控制资源并发
         *      3、keepAliveTime：存活时间。如果当前正在运行的线程数量大于核心数量（corePoolSize）。
         *          释放空闲的线程（maximumPoolSize - corePoolSize）。只要线程空闲大于指定的keepAliveTime；
         *      4、TimeUnit：时间单位
         *      5、BlockingQueue<Runnable> workQueue：阻塞队列。如果任务有很多，就会将目前多的任务放在队列里面。
         *          只要有线程空闲了，就会去队列里面取出新的任务继续执行
         *      6、threadFactory：线程的创建工厂。
         *      7、RejectedExecutionHandler handler：如果队列满了，按照我们指定的拒绝策略拒绝执行任务
         *
         * 工作顺序：
         *      1）、线程池创建，准备好corePoolSize数量的核心线程，准备接受任务
         *      1.1）、corePoolSize满了，就将再进来的任务放入阻塞队列中。空闲的corePoolSize就会自己去阻塞队列获取任务执行
         *      1.2）、阻塞队列满了，就直接开新线程执行，最大只能开到max指定的数量
         *      1.3）、max满了，就用RejectedExecutionHandler拒绝策略拒绝任务
         *      1.4）、max都执行完成，有很多空闲线程，在指定的keepAliveTime以后，释放max-core这些线程
         *      2）、所有的线程创建都是由指定的factory创建的
         *          new LinkedBlockingQueue<>()；默认是Integer的最大值。可能导致内存不足
         *
         *
         *  面试题：
         *      一个线程池 core 7; max 20; queue: 50, 100并发进来怎么分配？
         *   答：7个会立即得到执行，50个会进入队列再开13个进行执行。剩下的30个使用拒绝策略执行
         *
         *   拒绝策略：如果不想抛弃还要执行，可以使用CallerRunsPolicy
         */
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                5,
                200,
                10,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(100000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy());

        /**
         * 常见的四种线程池：
         *      1、newCachedThreadPool：创建一个可缓存线程池，如果线程池长度超过处理需要，可灵活回收空闲线程，若无可回收，则新建线程。
         *      2、newFixedThreadPool：创建一个定长线程池，可控制线程最大并发数，超出的线程会在队列中等待。
         *      3、newScheduledThreadPool：创建一个定长线程池，支持定时及周期性任务执行
         *      4、newSingleThreadExecutor：创建一个单线程化的线程池，它只会用唯一的工作线程来执行任务，保证所有任务
         */
//        Executors.newCachedThreadPool();    //core是0，所有都可回收
//        Executors.newFixedThreadPool();   //固定大小，core=max；都不可回收
//        Executors.newScheduledThreadPool()  //定时任务的线程池
//        Executors.newSingleThreadExecutor() //单线程的线程池，后台从队列里面获取任务，挨个执行
        System.out.println("main....end....");
    }

    public static class Thread01 extends Thread {
        @Override
        public void run () {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
        }
    }

    public static class Runable01 implements Runnable {
        @Override
        public void run() {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
        }
    }

    public static class Callable01 implements Callable<Integer> {
        @Override
        public Integer call() throws Exception {
            System.out.println("当前线程：" + Thread.currentThread().getId());
            int i = 10 / 2;
            System.out.println("运行结果：" + i);
            return i;
        }
    }
}
