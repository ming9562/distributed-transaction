package com.cym.distributed.transaction.core.enums;

/**
 * @author: YanmingChen
 * @date: 2019-08-06
 * @time: 16:14
 * @description: 事务中角色定义
 */
public enum TransactionRoleEnum {

    Originator("1", "发起者"),
    Participant("2", "参与者");

    private String code;
    private String name;

    private TransactionRoleEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static TransactionRoleEnum byCode(String code) {
        for (TransactionRoleEnum value : TransactionRoleEnum.values()) {
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
