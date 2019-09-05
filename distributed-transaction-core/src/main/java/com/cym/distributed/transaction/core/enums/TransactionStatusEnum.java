package com.cym.distributed.transaction.core.enums;

/**
 * @author: YanmingChen
 * @date: 2019-08-06
 * @time: 18:37
 * @description: 事务状态
 */
public enum  TransactionStatusEnum {

    unCommit("1", "未提交"),
    waitCommit("2", "等待提交"),
    preCommit("3", "预提交"),
    throwException("4", "出现异常"),
    commited("5", "已提交");

    private String code;
    private String name;

    private TransactionStatusEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static TransactionStatusEnum byCode(String code) {
        for (TransactionStatusEnum value : TransactionStatusEnum.values()) {
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
