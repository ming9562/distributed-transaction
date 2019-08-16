package com.yanmingchen.distributed.transaction.core.rabbitmq.test;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

//@Configuration
public class RabbitmqConfiguration {  
  
    @Autowired
    private RabbitTemplate rabbitTemplate;
  
    @PostConstruct
    public void initRabbitTemplate() {  
        // 设置生产者消息确认  
        rabbitTemplate.setConfirmCallback(new RabbitConfirmCallback());  
    }  
  
    /** 
     * 申明队列 
     * 
     * @return 
     */  
    @Bean
    public Queue queue() {
        Map<String, Object> arguments = new HashMap<>(4);
        // 申明死信交换器  
        arguments.put("x-dead-letter-exchange", "exchange-dlx");  
        return new Queue("queue-rabbit-springboot-advance", true, false, false, arguments);  
    }  
  
    /** 
     * 没有路由到的消息将进入此队列 
     * 
     * @return 
     */  
    @Bean  
    public Queue unRouteQueue() {  
        return new Queue("queue-unroute");  
    }  
  
    /** 
     * 死信队列 
     * 
     * @return 
     */  
    @Bean  
    public Queue dlxQueue() {  
        return new Queue("dlx-queue");  
    }  
  
    /** 
     * 申明交换器 
     * 
     * @return 
     */  
    @Bean  
    public Exchange exchange() {
        Map<String, Object> arguments = new HashMap<>(4);  
        // 当发往exchange-rabbit-springboot-advance的消息,routingKey和bindingKey没有匹配上时，将会由exchange-unroute交换器进行处理  
        arguments.put("alternate-exchange", "exchange-unroute");  
        return new DirectExchange("exchange-rabbit-springboot-advance", true, false, arguments);
    }  
  
    @Bean  
    public FanoutExchange unRouteExchange() {
        // 此处的交换器的名字要和 exchange() 方法中 alternate-exchange 参数的值一致  
        return new FanoutExchange("exchange-unroute");  
    }  
  
    /** 
     * 申明死信交换器 
     * 
     * @return 
     */  
    @Bean  
    public FanoutExchange dlxExchange() {  
        return new FanoutExchange("exchange-dlx");  
    }  
  
    /** 
     * 申明绑定 
     * 
     * @return 
     */  
    @Bean  
    public Binding binding() {
        return BindingBuilder.bind(queue()).to(exchange()).with("product").noargs();
    }  
  
    @Bean  
    public Binding unRouteBinding() {  
        return BindingBuilder.bind(unRouteQueue()).to(unRouteExchange());  
    }  
  
    @Bean  
    public Binding dlxBinding() {  
        return BindingBuilder.bind(dlxQueue()).to(dlxExchange());  
    }  
}