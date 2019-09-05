package com.cym.distributed.transaction.demo.ui;

import com.cym.distributed.transaction.core.rabbitmq.test.RabbitConsumer;
import com.cym.distributed.transaction.core.rabbitmq.test.RabbitProducer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author: YanmingChen
 * @date: 2019-08-08
 * @time: 10:29
 * @description:
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class RabbitmqTest {

    @Autowired
    private RabbitProducer rabbitProducer;

    @Autowired
    private RabbitConsumer rabbitConsumer;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void sendTest() {
        rabbitTemplate.convertAndSend("aaa", "测试一");
    }

}
