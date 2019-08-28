package com.yanmingchen.distributed.transaction.core.enums;

/**
 * @author: YanmingChen
 * @date: 2019-08-16
 * @time: 14:38
 * @description: 分布式事务类型枚举
 */
public enum  TransactionTypeEnum {

    TWO_PC("1", "两阶段提交"),
    TCC("2", "补偿事务");

    private String code;
    private String name;

    private TransactionTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static TransactionTypeEnum byCode(String code) {
        for (TransactionTypeEnum value : TransactionTypeEnum.values()) {
            if (value.getCode().equals(code)) {
                return value;
            }
        }
        return null;
    }

    public String getCode() {
        return this.code;
    }

    public String getName() {
        return this.name;
    }

}
