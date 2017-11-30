package com.sgsl.foodsee.cloud;

import com.sgsl.foodsee.cloud.lastmodify.EnableLastModifyProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import springfox.documentation.swagger2.annotations.EnableSwagger2;


@SpringBootApplication
@EnableEurekaClient
@EnableSwagger2
@EnableLastModifyProvider
public class LastmodifyDataExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(LastmodifyDataExampleApplication.class, args);
    }
}
