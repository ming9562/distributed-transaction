package com.cym.distributed.transaction.demo.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@ServletComponentScan
@EnableTransactionManagement
@ComponentScan("com.cym.distributed.transaction")
@MapperScan("com.cym.distributed.transaction.demo.order.mapper")
public class DistributedTransactionDemoOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(DistributedTransactionDemoOrderApplication.class, args);
    }

}
