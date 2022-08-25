package com.zc.gulimall.order.config;

import com.rabbitmq.client.Channel;
import com.zc.gulimall.order.entity.OrderEntity;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MyMQConfig {

    //@Bean Binding，Queue，Exchange

    /**
     * 延时/死信队列
     * 容器中的Binding，Queue，Exchange都会自动创建（RabbitMQ没有的情况）
     * RabbitMQ 只要有。@Bean声明属性发生变化也不会覆盖
     * @return
     */
    @Bean
    public Queue orderDelayQueue() {
        //public Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, @Nullable Map<String, Object> arguments);
        //队列名称，是否持久化，是否排他，是否自动删除，自定义属性
        /**
         * x-dead-letter-exchange: order-event-exchange
         * x-dead-letter-routing-key: order.release.order
         * x-message-ttl: 60000
         */
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "order-event-exchange");
        arguments.put("x-dead-letter-routing-key", "order.release.order");
        arguments.put("x-message-ttl", 60000);

        return new Queue("order.delay.queue", true, false, false, arguments);
    }

    /**
     * 延时/死信队列处理队列
     * @return
     */
    @Bean
    public Queue orderReleaseOrderQueue() {
        //order.release.order.queue
        return new Queue("order.release.order.queue", true, false, false);
    }

    /**
     * 延时队列交换机
     * @return
     */
    @Bean
    public Exchange orderEventExchange() {
        //public TopicExchange(String name, boolean durable, boolean autoDelete);
        //名称，是否持久化，是否自动删除，自定义参数
        return new TopicExchange("order-event-exchange", true, false);
    }

    /**
     * 绑定关系
     * @return
     */
    @Bean
    public Binding orderCreateOrderBinding() {
        // public Binding(String destination, DestinationType destinationType, String exchange, String routingKey, @Nullable Map<String, Object> arguments);
        //目的地，目的地类型，
        return new Binding("order.delay.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.create.order",
                null);
    }

    /**
     * 绑定关系
     * @return
     */
    @Bean
    public Binding orderReleaseOrderBinding() {
        // public Binding(String destination, DestinationType destinationType, String exchange, String routingKey, @Nullable Map<String, Object> arguments);
        //目的地，目的地类型，
        return new Binding("order.release.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.order",
                null);
    }

    /**
     * 订单释放直接和库存释放进行绑定
     * @return
     */
    @Bean
    public Binding orderReleaseOtherBinding() {
        // public Binding(String destination, DestinationType destinationType, String exchange, String routingKey, @Nullable Map<String, Object> arguments);
        //目的地，目的地类型，
        return new Binding("stock.release.stock.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.release.other.#",
                null);
    }

    @Bean
    public Queue OrderSeckillOrderQueue() {
        //public Queue(String name, boolean durable, boolean exclusive, boolean autoDelete, @Nullable Map<String, Object> arguments);
        return new Queue("order.seckill.order.queue", true, false, false);
    }

    @Bean
    public Binding OrderSeckillOrderQueueBinding() {
        // public Binding(String destination, DestinationType destinationType, String exchange, String routingKey, @Nullable Map<String, Object> arguments);
        return new Binding("order.seckill.order.queue",
                Binding.DestinationType.QUEUE,
                "order-event-exchange",
                "order.seckill.order",
                null);
    }
}
