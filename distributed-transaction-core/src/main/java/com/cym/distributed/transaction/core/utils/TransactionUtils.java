package com.cym.distributed.transaction.core.utils;

import java.util.Map;

/**
 * @author: YanmingChen
 * @date: 2019-08-16
 * @time: 10:00
 * @description:
 */
public class TransactionUtils {

    private static final ThreadLocal<Map<String, Object>> THREAD_LOCAL = new ThreadLocal<>();

    public static void set(Map<String, Object> map) {
        THREAD_LOCAL.set(map);
    }

    public static Map<String, Object> get() {
        return THREAD_LOCAL.get();
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }

}
