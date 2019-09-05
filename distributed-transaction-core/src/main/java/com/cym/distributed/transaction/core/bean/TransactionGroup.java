package com.cym.distributed.transaction.core.bean;

import com.cym.distributed.transaction.core.enums.TransactionTypeEnum;

import java.io.Serializable;
import java.util.Map;

import lombok.Data;

/**
 * @author: YanmingChen
 * @date: 2019-08-06
 * @time: 18:50
 * @description:
 */
@Data
public class TransactionGroup implements Serializable {

    /**
     * 组id
     */
    private String groupId;

    /**
     * 超时时间
     */
    private long timeout;

    /**
     * 事务操作
     */
    private String action;

    /**
     * 分布式事务类型
     */
    private TransactionTypeEnum transactionType;

    /**
     * 是否抛异常
     */
    private boolean throwException;

    /**
     * 事务组中出现的异常
     */
    private Class throwableClass;

    /**
     * 组内各节点
     */
    private Map<String, TransactionItem> itemMap;

}
