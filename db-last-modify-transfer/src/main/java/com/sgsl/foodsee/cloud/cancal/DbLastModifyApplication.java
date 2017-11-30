package com.sgsl.foodsee.cloud.cancal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Created by maoxianzhi.
 * CreateTime: 2017/10/23
 * ModifyBy  maoxianzhi
 * ModifyTime: 2017/10/23
 * Description:
 */

@SpringBootApplication
@EnableEurekaClient
@EnableCancalClient
@EnableMongoRepositories
public class DbLastModifyApplication {
    public static void main(String[] args) {
        SpringApplication.run(DbLastModifyApplication.class, args);
    }
}
