package com.sgsl.foodsee.cloud.lastmodify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @author maoxianzhi on 17.10.13.
 */

@Slf4j
@Configuration
@EnableConfigurationProperties(LastModifyMetaDataProperties.class)
public class LastModifyApiAutoConfiguration {

    @Bean
    public LastModifyMetaDataProvider lastModifyMetaDataProvider(LastModifyMetaDataProperties lastModifyMetaDataProperties) throws LastModifyMetaDataException {
        return new LastModifyMetaDataProvider(lastModifyMetaDataProperties);
    }

    @Bean
    public LastModifyMetaDataEndpoint lastModifyMetaDataEndpoint(LastModifyMetaDataProvider lastModifyMetaDataProvider, Environment environment) {
        log.debug("startting LastModifyMetaDataEndpoint");
        return new LastModifyMetaDataEndpoint(lastModifyMetaDataProvider, environment);
    }
}

