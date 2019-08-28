package com.yanmingchen.distributed.transaction.core.annotation;

import com.yanmingchen.distributed.transaction.core.enums.TransactionTypeEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author: YanmingChen
 * @date: 2019-07-22
 * @time: 15:00
 * @description: CYM分布式事务注解
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface CYMTransaction {

    /**
     * 事务管理器名称
     * @return
     */
    String txManager() default "";

    /**
     * 超时时间
     * 默认10秒
     * @return
     */
    long timeout() default 10000;

    /**
     * 取消方法
     * @return
     */
    String cancelMethod() default "";

    /**
     * 分布式事务类型
     * @return
     */
    TransactionTypeEnum transactionType() default TransactionTypeEnum.TCC;

}
