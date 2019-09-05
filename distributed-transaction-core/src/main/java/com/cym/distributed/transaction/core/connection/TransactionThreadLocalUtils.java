package com.cym.distributed.transaction.core.connection;

import com.cym.distributed.transaction.core.constant.CommonConstant;
import com.cym.distributed.transaction.core.utils.CommonUtils;

import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: YanmingChen
 * @date: 2019-08-06
 * @time: 15:30
 * @description:
 */
public class TransactionThreadLocalUtils {

    private static final ThreadLocal<Map<String, String>> THREAD_LOCAL = new ThreadLocal<>();

    public static Map<String, String> get() {
        Map<String, String> map = THREAD_LOCAL.get();
        if (CollectionUtils.isEmpty(map)) {
            map = new HashMap<>(2);
            map.put(CommonConstant.TRANSACTION_ID, String.valueOf(CommonUtils.getWorkerId()));
            map.put(CommonConstant.GROUP_ID, String.valueOf(CommonUtils.getWorkerId()));
            THREAD_LOCAL.set(map);
        }
        return map;
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }

}
