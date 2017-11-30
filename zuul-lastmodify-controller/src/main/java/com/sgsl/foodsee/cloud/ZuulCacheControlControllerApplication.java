package com.sgsl.foodsee.cloud;

import com.sgsl.foodsee.cloud.lastmodify.LastModifyMetaDataProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;


@SpringCloudApplication
@EnableZuulProxy
@EnableEurekaClient
public class ZuulCacheControlControllerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZuulCacheControlControllerApplication.class, args);
    }

    @Bean
    public LastModifyDataProvider lastModifyDataProvider(MongoTemplate mongoTemplate) {
        return new MongoLastModifyDataProvider(mongoTemplate);
    }

    @Bean
    public CacheControlPreControllerZuulFilter lastmodifyPreControllerZuulFilter(
            LastModifyDataProvider lastModifyDataProvider,
            LastModifyMetaDataProvider lastModifyMetaDataProvider) {
        return new CacheControlPreControllerZuulFilter(lastModifyDataProvider, lastModifyMetaDataProvider);
    }

    @Bean
    public CacheControlPostControllerZuulFilter lastmodifyPostControllerZuulFilter() {
        return new CacheControlPostControllerZuulFilter();
    }
}
