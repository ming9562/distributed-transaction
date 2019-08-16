package com.yanmingchen.distributed.transaction.demo.stock.config;

import com.alibaba.druid.pool.DruidDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * @Author: Xnn
 * @Date: 2019/7/3 10:53
 */
@Configuration
public class DruidConfig {

    private Logger logger = LoggerFactory.getLogger(DruidConfig.class);

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.druid.initial-size}")
    private int initialSize;

    @Value("${spring.datasource.druid.min-idle}")
    private int minIdle;

    @Value("${spring.datasource.druid.max-active}")
    private int maxActive;

    @Value("${spring.datasource.filters}")
    private String filters;


    /**
     * @Bean 声明，DataSource 对象为 Spring 容器所管理;
     * @Primary 表示这里定义的DataSource将覆盖其他来源的DataSource。 StatFilter，用于统计监控信息。StatFilter的别名是stat。
     * 统计SQL信息，合并统计。mergeStat是的MergeStatFilter缩写。 通过 DataSource 的属性<property name="filters" value="mergeStat" /> 或者
     * connectProperties属性来打开mergeSql功能 <property name="connectionProperties" value="druid.stat.mergeSql=true" />
     * StatFilter属性slowSqlMillis用来配置SQL慢的标准
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        DruidDataSource datasource = new DruidDataSource();
        datasource.setUrl(dbUrl);
        datasource.setDriverClassName(driverClassName);
        datasource.setUsername(username);
        datasource.setPassword(password);

        // configuration
        datasource.setInitialSize(initialSize);
        datasource.setMinIdle(minIdle);
        datasource.setMaxActive(maxActive);
        try {
            // 设置StatFilter，用于统计监控信息。StatFilter的别名是stat
            datasource.setFilters(filters);
        } catch (SQLException e) {
            logger.error("druid configuration initialization filter : {0}", e);
        }

        return datasource;
    }

}