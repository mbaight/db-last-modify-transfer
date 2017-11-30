package com.sgsl.foodsee.cloud.cancal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Created by maoxianzhi.
 * CreateTime: 2017/10/25
 * ModifyBy  maoxianzhi
 * ModifyTime: 2017/10/25
 * Description:
 */

@Slf4j
@Configuration
@EnableConfigurationProperties(CancalClientProperties.class)
public class CancalClientAutoConfiguration {
    @Bean
    @SuppressWarnings("unchecked")
    public CancalClientInstance cancalClientInstance(CancalClientProperties cancalClientProperties, MongoTemplate mongoTemplate) {
        return new CancalClientInstance(cancalClientProperties, mongoTemplate);
    }
}
