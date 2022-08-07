package com.zc.gulimall.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.zc.gulimall.order.entity.OrderEntity;
import com.zc.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zc.common.utils.PageUtils;
import com.zc.common.utils.Query;

import com.zc.gulimall.order.dao.OrderItemDao;
import com.zc.gulimall.order.entity.OrderItemEntity;
import com.zc.gulimall.order.service.OrderItemService;

@RabbitListener(queues = {"hello-java-queue"})
@Slf4j
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * queues：声明需要监听的所有队列
     *
     * org.springframework.amqp.core.Message
     *
     * 参数可以写以下类型
     * 1、Message message：原生消息详细信息。消息头+消息体
     * 2、T<发送的消息类型>
     * 3、Channel channel：当前传输数据的通道
     *
     * 队列Queue：可以很多人都来监听。只要收到消息，队列就会删除消息，而且只能有一个收到此消息
     * 场景：
     *      1）、订单服务启动多个；同一个消息只能有一个客户端收到
     *      2）、只有一个消息完全处理完，方法运行结束，我们就可以接收到下一个消息
     */
//    @RabbitListener(queues = {"hello-java-queue"})
    @RabbitHandler
    public void receiveMessage(Message message, OrderReturnReasonEntity content, Channel channel) throws InterruptedException {
        //{"id":1,"name":"哈哈","sort":null,"status":null,"createTime":1659858910520}
        byte[] body = message.getBody();
        //消息头属性信息
        MessageProperties messageProperties = message.getMessageProperties();
        log.info("接收到消息{}，内容{}", message, content);
//        Thread.sleep(3000);
        log.info("消息处理完成{}", content.getName());
        //channel内按顺序自增的
        long deliveryTag = messageProperties.getDeliveryTag();

        //签收货物，非批量签收模式
        try {
            if(deliveryTag % 2 == 0) {
                //收获
                channel.basicAck(deliveryTag, false);
                log.info("签收了货物...{}", deliveryTag);
            } else {
                //退货 var4：false丢弃true发回服务器，服务器重新入队
                //void basicNack(long var1, boolean var3, boolean var4) throws IOException;
                channel.basicNack(deliveryTag, false, true);
                //void basicReject(long var1, boolean var3) throws IOException;
//                channel.basicReject(deliveryTag, );
                log.info("没有签收了货物...{}", deliveryTag);
            }

        } catch (IOException e) {
            //网络中断
        }
    }

    @RabbitHandler
    public void receiveMessage2(Message message, OrderEntity content, Channel channel) throws InterruptedException {
        //{"id":1,"name":"哈哈","sort":null,"status":null,"createTime":1659858910520}
        log.info("接收到消息{}，内容{}", message, content);
    }
}