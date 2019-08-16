package com.yanmingchen.distributed.transaction.core.rabbitmq;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * @author: YanmingChen
 * @date: 2019-08-08
 * @time: 14:12
 * @description:
 */
public class ConnectionFactoryUtil {

    public static Connection getRabbitConnection() {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername(Config.USER_NAME);
        factory.setPassword(Config.PASSWORD);
        factory.setVirtualHost(Config.V_HOST);
        factory.setHost(Config.HOST);
        factory.setPort(Config.PORT);
        Connection conn = null;
        try {
            conn = factory.newConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }

}
