package com.yanmingchen.distributed.transaction.core.utils;

/**
 * @author: YanmingChen
 * @date: 2019-08-06
 * @time: 19:23
 * @description:
 */
public class CommonUtils {

    public static Long getWorkerId() {
        SnowflakeIdWorker idWorker = new SnowflakeIdWorker(0, 0);
        return idWorker.nextId();
    }

}
