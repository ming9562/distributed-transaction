package com.cym.distributed.transaction.core.rabbitmq;

import com.cym.distributed.transaction.core.bean.TransactionItem;
import com.cym.distributed.transaction.core.constant.CacheConstant;
import com.cym.distributed.transaction.core.enums.TransactionActionEnum;
import com.cym.distributed.transaction.core.enums.TransactionTypeEnum;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: YanmingChen
 * @date: 2019-08-08
 * @time: 14:27
 * @description:
 */
@Component
@Slf4j
public class RabbitConsumerUtil implements ApplicationContextAware {

    private static CachingConnectionFactory cachingConnectionFactory;

    @Autowired
    public void setCachingConnectionFactory(CachingConnectionFactory cachingConnectionFactory) {
        RabbitConsumerUtil.cachingConnectionFactory = cachingConnectionFactory;
    }

    private static RedisTemplate redisTemplate;

    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        RabbitConsumerUtil.redisTemplate = redisTemplate;
    }

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        RabbitConsumerUtil.applicationContext = applicationContext;
    }

    public static void listenTransactionAction(String queueName, TransactionStatus transactionStatus, PlatformTransactionManager platformTransactionManager, List<TransactionSynchronization> synchronizations, Map<Object, Object> resourceMap, TransactionTypeEnum transactionType) {
        String[] split = queueName.split(":");
        String groupId = split[0];
        String transactionId = split[1];
        log.info("监听队列：{}", groupId);

        try {
            // 创建一个连接
            Connection conn = cachingConnectionFactory.getRabbitConnectionFactory().newConnection();
            if (conn != null) {
                // 创建通道
                Channel channel = conn.createChannel();
                //广播
                channel.exchangeDeclare(transactionId, "fanout");
                // 声明队列【参数说明：参数一：队列名称，参数二：是否持久化；参数三：是否独占模式；参数四：消费者断开连接时是否删除队列；参数五：消息其他参数】
                channel.queueDeclare(groupId, false, false, false, null);

                // 创建订阅器，并接受消息
                channel.basicConsume(groupId, false, "", new DefaultConsumer(channel) {
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        if (!CollectionUtils.isEmpty(synchronizations)) {
                            boolean active = TransactionSynchronizationManager.isSynchronizationActive();
                            if (active) {
                                TransactionSynchronizationManager.clearSynchronization();
                            }
                            TransactionSynchronizationManager.initSynchronization();
                            for (TransactionSynchronization synchronization : synchronizations) {
                                TransactionSynchronizationManager.registerSynchronization(synchronization);
                            }
                        }
                        if (!CollectionUtils.isEmpty(resourceMap)) {
                            for (Map.Entry<Object, Object> entry : resourceMap.entrySet()) {
                                TransactionSynchronizationManager.bindResource(entry.getKey(), entry.getValue());
                            }
                        }

                        log.info("{}:{}", transactionStatus, platformTransactionManager);
                        try {
//                        String routingKey = envelope.getRoutingKey(); // 队列名称
//                        String contentType = properties.getContentType(); // 内容类型
                            // 消息正文
                            String content = new String(body, "utf-8");

                            // 如果通知事务回滚，调用cancel方法，如果正常，不做任何操作
                            switch (transactionType) {
                                case TCC:
                                    if (TransactionActionEnum.rollback.getCode().equals(content)) {
                                        log.info("TCC事务回滚。。。{}", queueName);
                                        TransactionItem item = (TransactionItem) redisTemplate.opsForHash().get(CacheConstant.TX_GROUP_ + groupId, transactionId);
                                        // 非出现异常的节点才回滚
                                        String cancelTargetClassName = item.getTargetClass();
                                        String targetMethod = item.getTargetMethod();
//                                        Object[] args = item.getTargetMethodArgsArr();
//                                        List<String> targetMethodArgsClassList = item.getTargetMethodArgsClassList();
//                                        List<Class<?>> classList = new ArrayList<>(targetMethodArgsClassList.size());
//                                        for (String classStr : targetMethodArgsClassList) {
//                                            Class<?> clazz = Class.forName(classStr);
//                                            classList.add(clazz);
//                                        }
//                                        Class<?>[] classArr = (Class<?>[]) classList.toArray();
//                                        Class cancelTargetClass = Class.forName(cancelTargetClassName);
//                                        String cancelMethodName = item.getCancelMethod();
//                                        Method method = cancelTargetClass.getMethod(cancelMethodName, classArr);
//                                        Object cancelTarget = applicationContext.getBean(cancelTargetClass);
//                                        method.invoke(cancelTarget, args);
                                    } else if (TransactionActionEnum.commit.getCode().equals(content)) {
                                        log.info("TCC事务提交。。。{}", queueName);
                                    }
                                    log.info("============TCC事务结束============");
                                    break;
                                case TWO_PC:
                                    if (TransactionActionEnum.rollback.getCode().equals(content)) {
                                        log.info("2PC事务回滚。。。{}", queueName);
                                        // 回滚事务
                                        platformTransactionManager.rollback(transactionStatus);
                                    } else if (TransactionActionEnum.commit.getCode().equals(content)) {
                                        log.info("2PC事务提交。。。{}", queueName);
                                        // 提交事务
                                        platformTransactionManager.commit(transactionStatus);
                                    }
                                    log.info("============2PC事务结束============");
                                    break;
                                default:
                                    break;
                            }

                            // 手动确认消息【参数说明：参数一：该消息的index；参数二：是否批量应答，true批量确认小于index的消息】
                            channel.basicAck(envelope.getDeliveryTag(), false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
