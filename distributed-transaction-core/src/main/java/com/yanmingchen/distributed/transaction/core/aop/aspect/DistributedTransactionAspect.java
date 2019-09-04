package com.yanmingchen.distributed.transaction.core.aop.aspect;

import com.yanmingchen.distributed.transaction.core.annotation.CYMTransaction;
import com.yanmingchen.distributed.transaction.core.connection.TransactionThreadLocalUtils;
import com.yanmingchen.distributed.transaction.core.constant.CacheConstant;
import com.yanmingchen.distributed.transaction.core.constant.CommonConstant;
import com.yanmingchen.distributed.transaction.core.enums.TransactionActionEnum;
import com.yanmingchen.distributed.transaction.core.enums.TransactionRoleEnum;
import com.yanmingchen.distributed.transaction.core.enums.TransactionStatusEnum;
import com.yanmingchen.distributed.transaction.core.enums.TransactionTypeEnum;
import com.yanmingchen.distributed.transaction.core.bean.TransactionGroup;
import com.yanmingchen.distributed.transaction.core.bean.TransactionItem;
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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.CollectionUtils;
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
    private RedisTemplate redisTemplate;

    @Pointcut("@annotation(com.yanmingchen.distributed.transaction.core.annotation.CYMTransaction)")
    private void transactionPointCut() {
    }

    @Around("transactionPointCut()")
    public Object doTransaction(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {
        // 获取注解属性
        Class<?> classTarget = proceedingJoinPoint.getTarget().getClass();
        String methodName = proceedingJoinPoint.getSignature().getName();
        Class<?>[] par = ((MethodSignature) proceedingJoinPoint.getSignature()).getParameterTypes();
        Method objMethod = classTarget.getMethod(methodName, par);
        CYMTransaction cymTransaction = objMethod.getAnnotation(CYMTransaction.class);
        String transactionManager = cymTransaction.transactionManager();
        String cancelMethod = cymTransaction.cancelMethod();
        TransactionTypeEnum transactionType = cymTransaction.transactionType();
        Isolation isolation = cymTransaction.isolation();
        Propagation propagation = cymTransaction.propagation();
        boolean readOnly = cymTransaction.readOnly();
        int timeout = cymTransaction.timeout();
        Class<? extends Throwable>[] rollbackFor = cymTransaction.rollbackFor();
        String[] rollbackForClassName = cymTransaction.rollbackForClassName();
        Class<? extends Throwable>[] noRollbackFor = cymTransaction.noRollbackFor();
        String[] noRollbackForClassName = cymTransaction.noRollbackForClassName();

        // 配置事务模板属性
        TransactionTemplate transactionTemplate = new TransactionTemplate();
        transactionTemplate.setName(transactionManager);
        transactionTemplate.setIsolationLevel(isolation.value());
        transactionTemplate.setIsolationLevelName(DefaultTransactionDefinition.PREFIX_ISOLATION + isolation.name());
        transactionTemplate.setPropagationBehavior(propagation.value());
        transactionTemplate.setPropagationBehaviorName(DefaultTransactionDefinition.PREFIX_PROPAGATION + propagation.name());
        transactionTemplate.setReadOnly(readOnly);
        transactionTemplate.setTimeout(timeout);
        // 从事务管理器获取事务状态
        TransactionStatus transactionStatus = platformTransactionManager.getTransaction(transactionTemplate);

        Object returnValue = null;
        String groupId = null;
        String transactionId = null;
        try {
            Object[] args = proceedingJoinPoint.getArgs();

            // 获取传递的transactionId和groupId
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attrs.getRequest();
            transactionId = request.getHeader(CommonConstant.TRANSACTION_ID);
            groupId = request.getHeader(CommonConstant.GROUP_ID);

            // 配置本地事务属性
            TransactionItem item = new TransactionItem();
            item.setTransactionId(transactionId);
            item.setTimeout(timeout);
            item.setStatus(TransactionStatusEnum.unCommit.getCode());
            item.setTargetClass(classTarget.getName());
            item.setTargetMethod(methodName);
            item.setTargetMethodArgsArr(args);
            item.setTargetMethodArgsClassArr(par);
            item.setCancelMethod(cancelMethod);

            // 判断是否有transactionId和groupId，如果没有，表示是事务的发起者
            if (StringUtils.isEmpty(transactionId) && StringUtils.isEmpty(groupId)) {
                // 第一次没有则生成后绑定当前线程，方便restTemplate拿出来添加到头传递
                Map<String, String> map = TransactionThreadLocalUtils.get();
                transactionId = map.get(CommonConstant.TRANSACTION_ID);
                groupId = map.get(CommonConstant.GROUP_ID);

                item.setRole(TransactionRoleEnum.Originator.getCode());
                item.setTransactionId(transactionId);

                Map<String, TransactionItem> itemMap = new HashMap<>(1);
                itemMap.put(transactionId, item);
                redisTemplate.opsForHash().putAll(CacheConstant.TX_GROUP_ + groupId, itemMap);

                TransactionGroup group = new TransactionGroup();
                group.setGroupId(groupId);
                group.setTimeout(timeout);
                group.setAction(TransactionActionEnum.wait.getCode());
                group.setTransactionType(transactionType);
                group.setThrowException(false);
                redisTemplate.opsForHash().put(CacheConstant.TX_GROUP, groupId, group);
            } else {
                item.setRole(TransactionRoleEnum.Participant.getCode());

                // 如果已经存在groupId和transactionId，表示是事务的参与者
                Map<String, TransactionItem> itemMap = (Map<String, TransactionItem>) redisTemplate.opsForHash().entries(CacheConstant.TX_GROUP_ + groupId);
                itemMap.put(transactionId, item);
                redisTemplate.opsForHash().putAll(CacheConstant.TX_GROUP_ + groupId, itemMap);

                // 事务组
                TransactionGroup group = (TransactionGroup) redisTemplate.opsForHash().get(CacheConstant.TX_GROUP, groupId);
                transactionType = group.getTransactionType();
            }

            log.info("{}:{}", groupId, transactionId);
            if (TransactionTypeEnum.TCC.equals(transactionType)) {
                log.info("============TCC事务开始============");
            } else if (TransactionTypeEnum.TWO_PC.equals(transactionType)) {
                log.info("============2PC事务开始============");
            } else {
                throw new Exception("未知分布式事务类型");
            }

            // 执行业务操作
            returnValue = proceedingJoinPoint.proceed(args);

            // 提交事务
            doCommit(transactionType, transactionStatus, groupId, transactionId, null);
        } catch (Throwable throwable) {
            log.error("事务处理出异常", throwable);

            // 获取组中出现的异常
            TransactionGroup group = (TransactionGroup) redisTemplate.opsForHash().get(CacheConstant.TX_GROUP, groupId);
            boolean isThrowException = group.isThrowException();
            Class throwableClass = group.getThrowableClass();
            if (!isThrowException) {
                throwableClass = throwable.getClass();
            }

            // 事务是否已完成
            boolean transactionFinished = false;

            // 如果设置了回滚异常字节码，匹配到当前异常则回滚
            if (rollbackFor.length > 0) {
                for (Class<? extends Throwable> rollbackForClass : rollbackFor) {
                    if (transactionFinished) {
                        break;
                    }
                    if (throwableClass.equals(rollbackForClass)) {
                        // 回滚事务
                        doRollback(transactionType, transactionStatus, groupId, transactionId, throwable);
                        transactionFinished = true;
                    }
                }
            }

            // 如果设置了回滚异常字节码全限定类名，匹配到当前异常则回滚
            if (rollbackForClassName.length > 0) {
                for (String className : rollbackForClassName) {
                    if (transactionFinished) {
                        break;
                    }
                    if (throwableClass.getName().equals(className)) {
                        // 回滚事务
                        doRollback(transactionType, transactionStatus, groupId, transactionId, throwable);
                        transactionFinished = true;
                    }
                }
            }

            // 如果设置了不回滚异常字节码，匹配到当前异常则不会滚，提交事务
            if (noRollbackFor.length > 0) {
                for (Class<? extends Throwable> noRollbackForClass : noRollbackFor) {
                    if (transactionFinished) {
                        break;
                    }
                    if (throwableClass.equals(noRollbackForClass)) {
                        // 提交事务
                        doCommit(transactionType, transactionStatus, groupId, transactionId, throwable);
                        transactionFinished = true;
                    }
                }
            }

            // 如果设置了不回滚异常字节码全限定类名，匹配到当前异常则不会滚，提交事务
            if (noRollbackForClassName.length > 0) {
                for (String className : noRollbackForClassName) {
                    if (transactionFinished) {
                        break;
                    }
                    if (throwableClass.getName().equals(className)) {
                        // 提交事务
                        doCommit(transactionType, transactionStatus, groupId, transactionId, throwable);
                        transactionFinished = true;
                    }
                }
            }

            // 如果事务未完成，回滚事务
            if (!transactionFinished) {
                // 回滚事务
                doRollback(transactionType, transactionStatus, groupId, transactionId, throwable);
            }

            throw throwable;
        } finally {
            // 释放资源
            doRelease(transactionType);
        }

        return returnValue;
    }

    /**
     * 处理提交事务
     * @param transactionType
     * @param transactionStatus
     * @param groupId
     * @param transactionId
     */
    private void doCommit(TransactionTypeEnum transactionType, TransactionStatus transactionStatus, String groupId, String transactionId, Throwable throwable) {
        TransactionGroup group = (TransactionGroup) redisTemplate.opsForHash().get(CacheConstant.TX_GROUP, groupId);
        TransactionItem item = (TransactionItem)  redisTemplate.opsForHash().get(CacheConstant.TX_GROUP_ + groupId, transactionId);
        TransactionRoleEnum transactionRoleEnum = TransactionRoleEnum.byCode(item.getRole());
        switch (transactionRoleEnum) {
            case Originator:

                log.info("事务成功。。。");

                if (throwable != null) {
                    if (!group.isThrowException()) {
                        group.setThrowException(true);
                        group.setThrowableClass(throwable.getClass());
                    }
                }
                group.setAction(TransactionActionEnum.commit.getCode());
                redisTemplate.opsForHash().put(CacheConstant.TX_GROUP, groupId, group);

                item.setStatus(TransactionStatusEnum.commited.getCode());
                redisTemplate.opsForHash().put(CacheConstant.TX_GROUP_ + groupId, transactionId, item);

                Map<String, TransactionItem> itemMap = (Map<String, TransactionItem>) redisTemplate.opsForHash().entries(CacheConstant.TX_GROUP_ + groupId);
                if (TransactionTypeEnum.TCC.equals(transactionType)) {
                    // 找出所有预提交的节点，通知事务成功
                    for (Map.Entry<String, TransactionItem> entry : itemMap.entrySet()) {
                        TransactionItem transactionItem = entry.getValue();
                        if (TransactionStatusEnum.preCommit.getCode().equals(transactionItem.getStatus())) {
                            // 通知各节点事务成功
                            RabbitProducerUtil.sendTransactionAction(groupId + ":" + transactionItem.getTransactionId(), TransactionActionEnum.commit.getCode());
                        }
                    }
                } else if (TransactionTypeEnum.TWO_PC.equals(transactionType)) {
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
                if (throwable != null) {
                    if (!group.isThrowException()) {
                        group.setThrowException(true);
                        group.setThrowableClass(throwable.getClass());
                        redisTemplate.opsForHash().put(CacheConstant.TX_GROUP, groupId, group);
                    }
                }

                List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
                Map<Object, Object> resourceMap = TransactionSynchronizationManager.getResourceMap();
                if (TransactionTypeEnum.TCC.equals(transactionType)) {
                    item.setStatus(TransactionStatusEnum.preCommit.getCode());
                    redisTemplate.opsForHash().put(CacheConstant.TX_GROUP_ + groupId, transactionId, item);
                    log.info("{}:{}预提交", groupId, transactionId);
                } else if (TransactionTypeEnum.TWO_PC.equals(transactionType)) {
                    item.setStatus(TransactionStatusEnum.waitCommit.getCode());
                    redisTemplate.opsForHash().put(CacheConstant.TX_GROUP_ + groupId, transactionId, item);
                    log.info("{}:{}等待提交", groupId, transactionId);
                }
                // 打开一个mq监听事务结果信息
                RabbitConsumerUtil.listenTransactionAction(groupId + ":" + transactionId, transactionStatus, platformTransactionManager, synchronizations, resourceMap, transactionType);
            default:
                break;
        }

        if (TransactionTypeEnum.TCC.equals(transactionType)) {
            platformTransactionManager.commit(transactionStatus);
            log.info("TCC事务开始提交事务");
        } else if (TransactionTypeEnum.TWO_PC.equals(transactionType)) {
            log.info("2PC事务开始等待提交");
        }
    }

    /**
     * 处理回滚事务
     * @param transactionType
     * @param transactionStatus
     * @param groupId
     * @param transactionId
     */
    private void doRollback(TransactionTypeEnum transactionType, TransactionStatus transactionStatus, String groupId, String transactionId, Throwable throwable) {
        TransactionGroup group = (TransactionGroup) redisTemplate.opsForHash().get(CacheConstant.TX_GROUP, groupId);
        TransactionItem item = (TransactionItem)  redisTemplate.opsForHash().get(CacheConstant.TX_GROUP_ + groupId, transactionId);
        TransactionRoleEnum transactionRoleEnum = TransactionRoleEnum.byCode(item.getRole());
        switch (transactionRoleEnum) {
            case Originator:

                log.error("事务出异常。。。");

                if (throwable != null) {
                    if (!group.isThrowException()) {
                        group.setThrowException(true);
                        group.setThrowableClass(throwable.getClass());
                    }
                }
                group.setAction(TransactionActionEnum.rollback.getCode());
                redisTemplate.opsForHash().put(CacheConstant.TX_GROUP, groupId, group);

                item.setStatus(TransactionStatusEnum.throwException.getCode());
                redisTemplate.opsForHash().put(CacheConstant.TX_GROUP_ + groupId, transactionId, item);

                Map<String, TransactionItem> itemMap = (Map<String, TransactionItem>) redisTemplate.opsForHash().entries(CacheConstant.TX_GROUP_ + groupId);

                if (TransactionTypeEnum.TCC.equals(transactionType)) {
                    // 找出所有预提交的节点，调用取消方法
                    for (Map.Entry<String, TransactionItem> entry : itemMap.entrySet()) {
                        TransactionItem transactionItem = entry.getValue();
                        if (TransactionStatusEnum.preCommit.getCode().equals(transactionItem.getStatus())) {
                            // 通知预提交节点调用取消方法
                            RabbitProducerUtil.sendTransactionAction(groupId + ":" + transactionItem.getTransactionId() , TransactionActionEnum.rollback.getCode());
                        }
                    }
                } else if (TransactionTypeEnum.TWO_PC.equals(transactionType)) {
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
                if (throwable != null) {
                    if (!group.isThrowException()) {
                        group.setThrowException(true);
                        group.setThrowableClass(throwable.getClass());
                        redisTemplate.opsForHash().put(CacheConstant.TX_GROUP, groupId, group);
                    }
                }

                item.setStatus(TransactionStatusEnum.throwException.getCode());
                redisTemplate.opsForHash().put(CacheConstant.TX_GROUP_ + groupId, transactionId, item);
                break;
            default:
                break;
        }

        log.info("{}:{}回滚", groupId, transactionId);
        platformTransactionManager.rollback(transactionStatus);
    }

    /**
     * 释放资源
     * @param transactionType
     */
    private void doRelease(TransactionTypeEnum transactionType) {
        TransactionThreadLocalUtils.remove();
        if (TransactionTypeEnum.TCC.equals(transactionType)) {
            log.info("============TCC事务结束============");
        } else if (TransactionTypeEnum.TWO_PC.equals(transactionType)) {
            log.info("============2PC事务结束============");
        }
    }

}
