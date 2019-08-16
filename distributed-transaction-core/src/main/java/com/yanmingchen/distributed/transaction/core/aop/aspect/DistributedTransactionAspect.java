package com.yanmingchen.distributed.transaction.core.aop.aspect;

import com.yanmingchen.distributed.transaction.core.annotation.CYMTransaction;
import com.yanmingchen.distributed.transaction.core.connection.TransactionThreadLocalUtils;
import com.yanmingchen.distributed.transaction.core.constant.CacheConstant;
import com.yanmingchen.distributed.transaction.core.constant.CommonConstant;
import com.yanmingchen.distributed.transaction.core.enums.TransactionActionEnum;
import com.yanmingchen.distributed.transaction.core.enums.TransactionRoleEnum;
import com.yanmingchen.distributed.transaction.core.enums.TransactionStatusEnum;
import com.yanmingchen.distributed.transaction.core.enums.TransactionTypeEnum;
import com.yanmingchen.distributed.transaction.core.netty.bean.TransactionGroup;
import com.yanmingchen.distributed.transaction.core.netty.bean.TransactionItem;
import com.yanmingchen.distributed.transaction.core.rabbitmq.RabbitConsumerUtil;
import com.yanmingchen.distributed.transaction.core.rabbitmq.RabbitProducerUtil;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: YanmingChen
 * @date: 2019-08-06
 * @time: 14:27
 * @description:
 */
@Aspect
@Component
@Slf4j
public class DistributedTransactionAspect {

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Autowired
    private TransactionDefinition transactionDefinition;

    @Autowired
    private RedisTemplate redisTemplate;

    @Pointcut("@annotation(com.yanmingchen.distributed.transaction.core.annotation.CYMTransaction)")
    private void transactionPointCut() {
    }

    @Around("transactionPointCut()")
    public Object doTransaction(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        TransactionStatus transactionStatus = platformTransactionManager.getTransaction(transactionDefinition);

        Object returnValue = null;
        String groupId = null;
        String transactionId = null;
        CYMTransaction cymTransaction = null;
        TransactionTypeEnum transactionType = null;
        try {
            Class<?> classTarget = proceedingJoinPoint.getTarget().getClass();
            String methodName = proceedingJoinPoint.getSignature().getName();
            Class<?>[] par = ((MethodSignature) proceedingJoinPoint.getSignature()).getParameterTypes();
            Method objMethod = classTarget.getMethod(methodName, par);
            cymTransaction = objMethod.getAnnotation(CYMTransaction.class);
            transactionType = cymTransaction.transactionType();

            Object[] args = proceedingJoinPoint.getArgs();

            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attrs.getRequest();
            transactionId = request.getHeader(CommonConstant.TRANSACTION_ID);
            groupId = request.getHeader(CommonConstant.GROUP_ID);
            // 判断是否有transactionId和groupId，如果没有，表示是事务的发起者
            if (StringUtils.isEmpty(transactionId) && StringUtils.isEmpty(groupId)) {
                // 第一次没有则生成后绑定当前线程，方便restTemplate拿出来添加到头传递
                Map<String, String> map = TransactionThreadLocalUtils.get();
                transactionId = map.get(CommonConstant.TRANSACTION_ID);
                groupId = map.get(CommonConstant.GROUP_ID);

                Map<String, TransactionItem> itemMap = new HashMap<>(1);
                TransactionItem item = new TransactionItem();
                item.setTransactionId(transactionId);
                item.setTimeout(cymTransaction.timeout());
                // 事务发起者
                item.setRole(TransactionRoleEnum.Originator.getCode());
                item.setStatus(TransactionStatusEnum.unCommit.getCode());
                item.setTargetClass(classTarget.getName());
                item.setTargetMethod(methodName);
                item.setTargetMethodArgsArr(args);
                item.setTargetMethodArgsClassArr(par);
                item.setCancelMethod(cymTransaction.cancelMethod());
                itemMap.put(transactionId, item);
                redisTemplate.opsForHash().putAll(CacheConstant.TX_GROUP_ + groupId, itemMap);

                TransactionGroup group = new TransactionGroup();
                group.setGroupId(groupId);
                group.setTimeout(cymTransaction.timeout());
                group.setAction(TransactionActionEnum.wait.getCode());
                group.setTransactionType(transactionType);
                redisTemplate.opsForHash().put(CacheConstant.TX_GROUP, groupId, group);
            } else {
                // 如果已经存在groupId和transactionId，表示是事务的参与者
                Map<String, TransactionItem> itemMap = (Map<String, TransactionItem>) redisTemplate.opsForHash().entries(CacheConstant.TX_GROUP_ + groupId);
                TransactionItem item = new TransactionItem();
                item.setTransactionId(transactionId);
                item.setTimeout(cymTransaction.timeout());
                // 事务参与者
                item.setRole(TransactionRoleEnum.Participant.getCode());
                item.setStatus(TransactionStatusEnum.unCommit.getCode());
                item.setTargetClass(classTarget.getName());
                item.setTargetMethod(methodName);
                item.setTargetMethodArgsArr(args);
                item.setTargetMethodArgsClassArr(par);
                item.setCancelMethod(cymTransaction.cancelMethod());
                itemMap.put(transactionId, item);
                redisTemplate.opsForHash().putAll(CacheConstant.TX_GROUP_ + groupId, itemMap);
                // 事务组
                TransactionGroup group = (TransactionGroup) redisTemplate.opsForHash().get(CacheConstant.TX_GROUP, groupId);
                transactionType = group.getTransactionType();
            }

            log.info("{}:{}", groupId, transactionId);
            if (TransactionTypeEnum.tcc.equals(transactionType)) {
                log.info("============TCC事务开始============");
            } else if (TransactionTypeEnum.two_pc.equals(transactionType)) {
                log.info("============2PC事务开始============");
            } else {
                throw new Exception("未知分布式事务类型");
            }

            // 执行业务操作
            returnValue = proceedingJoinPoint.proceed(args);

            TransactionItem item = (TransactionItem)  redisTemplate.opsForHash().get(CacheConstant.TX_GROUP_ + groupId, transactionId);
            TransactionRoleEnum transactionRoleEnum = TransactionRoleEnum.byCode(item.getRole());
            switch (transactionRoleEnum) {
                case Originator:

                    log.info("事务成功。。。");

                    item.setStatus(TransactionStatusEnum.commited.getCode());
                    redisTemplate.opsForHash().put(CacheConstant.TX_GROUP_ + groupId, transactionId, item);

                    TransactionGroup group = (TransactionGroup) redisTemplate.opsForHash().get(CacheConstant.TX_GROUP, groupId);
                    group.setAction(TransactionActionEnum.commit.getCode());
                    redisTemplate.opsForHash().put(CacheConstant.TX_GROUP, groupId, group);

                    Map<String, TransactionItem> itemMap = (Map<String, TransactionItem>) redisTemplate.opsForHash().entries(CacheConstant.TX_GROUP_ + groupId);
                    if (TransactionTypeEnum.tcc.equals(transactionType)) {
                        // 找出所有预提交的节点，通知事务成功
                        for (Map.Entry<String, TransactionItem> entry : itemMap.entrySet()) {
                            TransactionItem transactionItem = entry.getValue();
                            if (TransactionStatusEnum.preCommit.getCode().equals(transactionItem.getStatus())) {
                                // 通知各节点事务成功
                                RabbitProducerUtil.sendTransactionAction(groupId + ":" + transactionItem.getTransactionId(), TransactionActionEnum.commit.getCode());
                            }
                        }
                    } else if (TransactionTypeEnum.two_pc.equals(transactionType)) {
                        // 找出所有准备提交的节点，通知提交事务
                        for (Map.Entry<String, TransactionItem> entry : itemMap.entrySet()) {
                            TransactionItem transactionItem = entry.getValue();
                            if (TransactionStatusEnum.waitCommit.getCode().equals(transactionItem.getStatus())) {
                                // 通知各节点提交事务
                                RabbitProducerUtil.sendTransactionAction(groupId + ":" + transactionItem.getTransactionId(), TransactionActionEnum.commit.getCode());
                            }
                        }
                    }

                    break;
                case Participant:
                    List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
                    Map<Object, Object> resourceMap = TransactionSynchronizationManager.getResourceMap();
                    if (TransactionTypeEnum.tcc.equals(transactionType)) {
                        item.setStatus(TransactionStatusEnum.preCommit.getCode());
                        redisTemplate.opsForHash().put(CacheConstant.TX_GROUP_ + groupId, transactionId, item);
                        log.info("{}:{}预提交", groupId, transactionId);
                    } else if (TransactionTypeEnum.two_pc.equals(transactionType)) {
                        item.setStatus(TransactionStatusEnum.waitCommit.getCode());
                        redisTemplate.opsForHash().put(CacheConstant.TX_GROUP_ + groupId, transactionId, item);
                        log.info("{}:{}等待提交", groupId, transactionId);
                    }
                    // 打开一个mq监听事务结果信息
                    RabbitConsumerUtil.listenTransactionAction(groupId + ":" + transactionId, transactionStatus, platformTransactionManager, synchronizations, resourceMap, transactionType);
                default:
                    break;
            }

            if (TransactionTypeEnum.tcc.equals(transactionType)) {
                platformTransactionManager.commit(transactionStatus);
                log.info("TCC事务开始提交事务");
            } else if (TransactionTypeEnum.two_pc.equals(transactionType)) {
                log.info("2PC事务开始等待提交");
            }
        } catch (Throwable throwable) {
            log.error("事务处理出异常", throwable);

            TransactionItem item = (TransactionItem)  redisTemplate.opsForHash().get(CacheConstant.TX_GROUP_ + groupId, transactionId);
            TransactionRoleEnum transactionRoleEnum = TransactionRoleEnum.byCode(item.getRole());
            switch (transactionRoleEnum) {
                case Originator:

                    log.error("事务出异常。。。");

                    item.setStatus(TransactionStatusEnum.throwException.getCode());
                    redisTemplate.opsForHash().put(CacheConstant.TX_GROUP_ + groupId, transactionId, item);

                    TransactionGroup group = (TransactionGroup) redisTemplate.opsForHash().get(CacheConstant.TX_GROUP, groupId);
                    group.setAction(TransactionActionEnum.rollback.getCode());
                    redisTemplate.opsForHash().put(CacheConstant.TX_GROUP, groupId, group);

                    Map<String, TransactionItem> itemMap = (Map<String, TransactionItem>) redisTemplate.opsForHash().entries(CacheConstant.TX_GROUP_ + groupId);

                    if (TransactionTypeEnum.tcc.equals(transactionType)) {
                        // 找出所有预提交的节点，调用取消方法
                        for (Map.Entry<String, TransactionItem> entry : itemMap.entrySet()) {
                            TransactionItem transactionItem = entry.getValue();
                            if (TransactionStatusEnum.preCommit.getCode().equals(transactionItem.getStatus())) {
                                // 通知预提交节点调用取消方法
                                RabbitProducerUtil.sendTransactionAction(groupId + ":" + transactionItem.getTransactionId() , TransactionActionEnum.rollback.getCode());
                            }
                        }
                    } else if (TransactionTypeEnum.two_pc.equals(transactionType)) {
                        // 找出所有等待提交的节点，通知事务回滚
                        for (Map.Entry<String, TransactionItem> entry : itemMap.entrySet()) {
                            TransactionItem transactionItem = entry.getValue();
                            if (TransactionStatusEnum.waitCommit.getCode().equals(transactionItem.getStatus())) {
                                // 通知准备提交的节点回滚事务
                                RabbitProducerUtil.sendTransactionAction(groupId + ":" + transactionItem.getTransactionId() , TransactionActionEnum.rollback.getCode());
                            }
                        }
                    }

                    break;
                case Participant:
                    item.setStatus(TransactionStatusEnum.throwException.getCode());
                    redisTemplate.opsForHash().put(CacheConstant.TX_GROUP_ + groupId, transactionId, item);
                    break;
                default:
                    break;
            }

            log.info("{}:{}回滚", groupId, transactionId);
            platformTransactionManager.rollback(transactionStatus);
            throw throwable;
        } finally {
            TransactionThreadLocalUtils.remove();
        }

        return returnValue;
    }

}
