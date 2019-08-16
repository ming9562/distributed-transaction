package com.yanmingchen.distributed.transaction.core.connection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Connection;

import javax.sql.DataSource;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: YanmingChen
 * @date: 2019-07-22
 * @time: 15:09
 * @description:
 */
@Component
@Slf4j
public class ConnectionUtils {

    /**
     * ThreadLocal，绑定当前线程的连接
     */
    private static final ThreadLocal<Connection> THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 连接池
     */
    public static DataSource dataSource;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        ConnectionUtils.dataSource = dataSource;
    }

    /**
     * 打开连接
     */
    public static Connection getConnection() {
        // 从ThreadLocal中获取当前线程绑定连接对象
        Connection connection = THREAD_LOCAL.get();
        if (connection == null) {
            try {
                log.info("从连接池中获取连接。。。");
                connection = dataSource.getConnection();
                THREAD_LOCAL.set(connection);
            } catch (Exception e) {
                log.error("连接池获取连接出异常", e);
            }
        }
        return connection;
    }

    /**
     * 开启事务
     */
    public static void openTransaction() {
        try {
            Connection connection = THREAD_LOCAL.get();
            if (connection != null) {
                log.info("开启事务。。。");
                connection.setAutoCommit(false);
            }
        } catch (Exception e) {
            log.error("开启出异常", e);
        }
    }

    /**
     * 提交事务
     */
    public static void commit() {
        try {
            Connection connection = THREAD_LOCAL.get();
            if (connection != null) {
                log.info("提交事务。。。");
                connection.commit();
            }
        } catch (Exception e) {
            log.error("提交事务出异常", e);
        }
    }

    /**
     * 回滚事务
     */
    public static void rollback() {
        try {
            Connection connection = THREAD_LOCAL.get();
            if (connection != null) {
                log.info("回滚事务。。。");
                connection.rollback();
            }
        } catch (Exception e) {
            log.error("回滚事务出异常", e);
        }
    }

    /**
     * 关闭连接
     */
    public static void close() {
        try {
            Connection connection = THREAD_LOCAL.get();
            if (connection != null) {
                log.info("关闭连接。。。");
                connection.close();
                THREAD_LOCAL.remove();
            }
        } catch (Exception e) {
            log.error("关闭连接出异常", e);
        }
    }

}
