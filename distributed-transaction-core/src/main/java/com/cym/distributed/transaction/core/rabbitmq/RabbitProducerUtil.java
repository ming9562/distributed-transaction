package com.cym.distributed.transaction.core.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: YanmingChen
 * @date: 2019-08-08
 * @time: 14:18
 * @description:
 */
@Component
@Slf4j
public class RabbitProducerUtil {

    private static CachingConnectionFactory cachingConnectionFactory;

    @Autowired
    public void setCachingConnectionFactory(CachingConnectionFactory cachingConnectionFactory) {
        RabbitProducerUtil.cachingConnectionFactory = cachingConnectionFactory;
    }

    public static void sendTransactionAction(String queueName, String content) {
        String[] split = queueName.split(":");
        String groupId = split[0];
        String transactionId = split[1];
        log.info("发送消息：{}", queueName);

        try {
            // 创建一个连接
            Connection conn = cachingConnectionFactory.getRabbitConnectionFactory().newConnection();
            if (conn != null) {
                // 创建通道
                Channel channel = conn.createChannel();
                channel.exchangeDeclare(transactionId, "fanout");//广播
                // 声明队列【参数说明：参数一：队列名称，参数二：是否持久化；参数三：是否独占模式；参数四：消费者断开连接时是否删除队列；参数五：消息其他参数】
                channel.queueDeclare(groupId, false, false, false, null);
                // 发送内容【参数说明：参数一：交换机名称；参数二：队列名称，参数三：消息的其他属性-routing headers，此属性为MessageProperties.PERSISTENT_TEXT_PLAIN用于设置纯文本消息存储到硬盘；参数四：消息主体】
                channel.basicPublish("", groupId, null, content.getBytes("UTF-8"));
                // 关闭连接
                channel.close();
                conn.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
