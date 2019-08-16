package com.yanmingchen.distributed.transaction.core.rabbitmq.test;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RabbitConfirmCallback implements RabbitTemplate.ConfirmCallback {
  
    @Override  
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        log.info("(start)生产者消息确认=========================");  
        log.info("correlationData:[{}]", correlationData);  
        log.info("ack:[{}]", ack);  
        log.info("cause:[{}]", cause);  
        if (!ack) {  
            log.info("消息可能未到达rabbitmq服务器");  
        }  
        log.info("(end)生产者消息确认=========================");  
    }

} 