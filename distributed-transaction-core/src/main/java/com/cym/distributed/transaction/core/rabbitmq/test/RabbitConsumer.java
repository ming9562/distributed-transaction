package com.cym.distributed.transaction.core.rabbitmq.test;

import com.rabbitmq.client.Channel;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Random;

import lombok.extern.slf4j.Slf4j;

//@Component
@Slf4j
public class RabbitConsumer {  
  
    /** 
     * 监听 queue-rabbit-springboot-advance 队列 
     * 
     * @param receiveMessage 接收到的消息 
     * @param message 
     * @param channel 
     */  
    @RabbitListener(queues = "queue-rabbit-springboot-advance")
    public void receiveMessage(String receiveMessage, Message message, Channel channel) {
        try {  
            // 手动签收  
            log.info("接收到消息:[{}]", receiveMessage);  
            if (new Random().nextInt(10) < 5) {
                log.warn("拒绝一条信息:[{}]，此消息将会由死信交换器进行路由.", receiveMessage);  
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);  
            } else {  
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);  
            }  
        } catch (Exception e) {  
            log.info("接收到消息之后的处理发生异常.", e);  
            try {  
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);  
            } catch (IOException e1) {
                log.error("签收异常.", e1);  
            }  
        }  
    }  
}