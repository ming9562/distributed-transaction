package com.yanmingchen.distributed.transaction.core.rabbitmq.test;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.IntStream;

import lombok.extern.slf4j.Slf4j;

//@Component
@Slf4j
public class RabbitProducer implements ApplicationListener<ContextRefreshedEvent> {
  
    @Autowired
    private RabbitTemplate rabbitTemplate;
  
    @Override  
    public void onApplicationEvent(ContextRefreshedEvent event) {  
        String exchange = "exchange-rabbit-springboot-advance";  
        String routingKey = "product";  
        String unRoutingKey = "norProduct";  
  
        // 1.发送一条正常的消息 CorrelationData唯一（可以在ConfirmListener中确认消息）  
        IntStream.rangeClosed(0, 10).forEach(num -> {
            String message = LocalDateTime.now().toString() + "发送第" + (num + 1) + "条消息.";
            rabbitTemplate.convertAndSend(exchange, routingKey, message, new CorrelationData("routing" + UUID.randomUUID().toString()));
            log.info("发送一条消息,exchange:[{}],routingKey:[{}],message:[{}]", exchange, routingKey, message);  
        });  
        // 2.发送一条未被路由的消息，此消息将会进入备份交换器（alternate exchange）  
        String message = LocalDateTime.now().toString() + "发送一条消息.";  
        rabbitTemplate.convertAndSend(exchange, unRoutingKey, message, new CorrelationData("unRouting-" + UUID.randomUUID().toString()));  
        log.info("发送一条消息,exchange:[{}],routingKey:[{}],message:[{}]", exchange, unRoutingKey, message);  
    }  
}