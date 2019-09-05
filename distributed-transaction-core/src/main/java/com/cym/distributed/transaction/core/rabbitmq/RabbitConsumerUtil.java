package com.cym.distributed.transaction.core.rabbitmq;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.cym.distributed.transaction.core.constant.CacheConstant;
import com.cym.distributed.transaction.core.enums.TransactionActionEnum;
import com.cym.distributed.transaction.core.enums.TransactionTypeEnum;
import com.cym.distributed.transaction.core.bean.TransactionItem;

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
                channel.exchangeDeclare(transactionId, "fanout");//广播
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

                        System.out.println(transactionStatus + "   " + platformTransactionManager);
                        try {
//                        String routingKey = envelope.getRoutingKey(); // 队列名称
//                        String contentType = properties.getContentType(); // 内容类型
                            String content = new String(body, "utf-8"); // 消息正文

                            // 如果通知事务回滚，调用cancel方法，如果正常，不做任何操作
                            if (TransactionActionEnum.rollback.getCode().equals(content)) {
                                if (TransactionTypeEnum.TCC.equals(transactionType)) {
                                    log.info("TCC事务回滚。。。{}", queueName);
                                    TransactionItem item = (TransactionItem) redisTemplate.opsForHash().get(CacheConstant.TX_GROUP_ + groupId, transactionId);
                                    // 非出现异常的节点才回滚
                                    String cancelTargetClassName = item.getTargetClass();
                                    String targetMethod = item.getTargetMethod();
                                    Object[] args = item.getTargetMethodArgsArr();
                                    Class<?>[] argsClassArr = item.getTargetMethodArgsClassArr();
                                    Class cancelTargetClass = Class.forName(cancelTargetClassName);
                                    String cancelMethodName = item.getCancelMethod();
                                    Method method = cancelTargetClass.getMethod(cancelMethodName, argsClassArr);
                                    Object cancelTarget = applicationContext.getBean(cancelTargetClass);
                                    method.invoke(cancelTarget, args);
                                } else if (TransactionTypeEnum.TWO_PC.equals(transactionType)) {
                                    log.info("2PC事务回滚。。。{}", queueName);
                                    // 回滚事务
                                    platformTransactionManager.rollback(transactionStatus);
                                }
                            } else if (TransactionActionEnum.commit.getCode().equals(content)) {
                                if (TransactionTypeEnum.TCC.equals(transactionType)) {
                                    log.info("TCC事务提交。。。{}", queueName);
                                } else if (TransactionTypeEnum.TWO_PC.equals(transactionType)) {
                                    log.info("2PC事务提交。。。{}", queueName);
                                    // 提交事务
                                    platformTransactionManager.commit(transactionStatus);
                                }
                            }

                            if (TransactionTypeEnum.TCC.equals(transactionType)) {
                                log.info("============TCC事务结束============");
                            } else if (TransactionTypeEnum.TWO_PC.equals(transactionType)) {
                                log.info("============2PC事务结束============");
                            }

                            channel.basicAck(envelope.getDeliveryTag(), false); // 手动确认消息【参数说明：参数一：该消息的index；参数二：是否批量应答，true批量确认小于index的消息】
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
