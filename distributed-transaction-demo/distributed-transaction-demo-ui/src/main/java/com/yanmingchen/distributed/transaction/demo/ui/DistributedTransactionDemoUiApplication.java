package com.yanmingchen.distributed.transaction.demo.ui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients
@ComponentScan("com.yanmingchen.distributed.transaction")
public class DistributedTransactionDemoUiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DistributedTransactionDemoUiApplication.class, args);
    }

}
