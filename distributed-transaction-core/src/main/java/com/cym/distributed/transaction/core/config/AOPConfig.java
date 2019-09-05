package com.cym.distributed.transaction.core.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * @author: YanmingChen
 * @date: 2019-08-06
 * @time: 14:29
 * @description:
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class AOPConfig {

}
