package com.yanmingchen.distributed.transaction.core.netty.bean;

import java.io.Serializable;

import lombok.Data;

/**
 * @author: YanmingChen
 * @date: 2019-08-06
 * @time: 18:50
 * @description:
 */
@Data
public class TransactionItem implements Serializable {

    /**
     * 节点id
     */
    private String transactionId;

    /**
     * 节点角色
     */
    private String role;

    /**
     * 事务状态
     */
    private String status;

    /**
     * 超时时间
     */
    private long timeout;

    /**
     * 执行目标类字节码
     */
    private String targetClass;

    /**
     * 目标方法
     */
    private String targetMethod;

    /**
     * 目标方法参数
     */
    private Object[] targetMethodArgsArr;

    /**
     * 目标方法参数字节码
     */
    private Class<?>[] targetMethodArgsClassArr;

    /**
     * 取消方法
     */
    private String cancelMethod;

}
