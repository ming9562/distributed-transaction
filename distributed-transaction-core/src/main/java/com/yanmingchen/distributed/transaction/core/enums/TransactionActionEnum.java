package com.yanmingchen.distributed.transaction.core.enums;

/**
 * @author: YanmingChen
 * @date: 2019-08-06
 * @time: 18:41
 * @description: 事务执行操作
 */
public enum  TransactionActionEnum {

    wait("1", "等待操作"),
    commit("2", "提交"),
    rollback("3", "回滚");

    private String code;
    private String name;

    private TransactionActionEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static TransactionActionEnum byCode(String code) {
        for (TransactionActionEnum value : TransactionActionEnum.values()) {
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
