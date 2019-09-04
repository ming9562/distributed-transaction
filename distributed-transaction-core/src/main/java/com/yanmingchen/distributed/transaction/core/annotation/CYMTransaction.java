package com.yanmingchen.distributed.transaction.core.annotation;

import com.yanmingchen.distributed.transaction.core.enums.TransactionTypeEnum;

import org.springframework.core.annotation.AliasFor;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;

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

    @AliasFor("transactionManager")
    String value() default "";

    /**
     * 事务管理器名称
     * @return
     */
    @AliasFor("value")
    String transactionManager() default "";

    /**
     * 超时时间
     * @return
     */
    int timeout() default TransactionDefinition.TIMEOUT_DEFAULT;

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

    /**
     * 事务的传播行为
     * @return
     */
    Propagation propagation() default Propagation.REQUIRED;

    /**
     * 事务的隔离级别
     * @return
     */
    Isolation isolation() default Isolation.DEFAULT;

    /**
     * 是否只读型事务
     * @return
     */
    boolean readOnly() default false;

    Class<? extends Throwable>[] rollbackFor() default {};

    String[] rollbackForClassName() default {};

    Class<? extends Throwable>[] noRollbackFor() default {};

    String[] noRollbackForClassName() default {};

}
