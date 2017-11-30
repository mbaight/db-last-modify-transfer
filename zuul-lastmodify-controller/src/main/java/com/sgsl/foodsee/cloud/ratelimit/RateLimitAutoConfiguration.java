package com.sgsl.foodsee.cloud.ratelimit;

import com.ecwid.consul.v1.ConsulClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.zuul.ZuulFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.consul.ConditionalOnConsulEnabled;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.util.UrlPathHelper;

/**
 * Created by Administrator .
 * create_time: 2017/11/30 0030
 * modify_time: 2017/11/30 0030
 */

@Configuration
@EnableConfigurationProperties(RateLimitProperties.class)
@ConditionalOnProperty(prefix = RateLimitProperties.PREFIX, name = "enabled", havingValue = "true")
public class RateLimitAutoConfiguration {

    private final UrlPathHelper urlPathHelper = new UrlPathHelper();

    @Bean
    public ZuulFilter rateLimiterPreFilter(final RateLimiter rateLimiter,
                                           final RateLimitProperties rateLimitProperties,
                                           final RouteLocator routeLocator,
                                           final RateLimitKeyGenerator rateLimitKeyGenerator) {
        return new RateLimitPreFilter(rateLimitProperties, routeLocator, urlPathHelper, rateLimiter,
                rateLimitKeyGenerator);
    }

    @Bean
    public ZuulFilter rateLimiterPostFilter(final RateLimiter rateLimiter,
                                            final RateLimitProperties rateLimitProperties,
                                            final RouteLocator routeLocator,
                                            final RateLimitKeyGenerator rateLimitKeyGenerator) {
        return new RateLimitPostFilter(rateLimitProperties, routeLocator, urlPathHelper, rateLimiter,
                rateLimitKeyGenerator);
    }

    @Bean
    @ConditionalOnMissingBean(RateLimitKeyGenerator.class)
    public RateLimitKeyGenerator ratelimitKeyGenerator(final RateLimitProperties properties) {
        return new DefaultRateLimitKeyGenerator(properties);
    }

    @ConditionalOnClass(RedisTemplate.class)
    @ConditionalOnMissingBean(RateLimiter.class)
    @ConditionalOnProperty(prefix = RateLimitProperties.PREFIX, name = "repository", havingValue = "REDIS")
    public static class RedisConfiguration {

        @Bean("rateLimiterRedisTemplate")
        public StringRedisTemplate redisTemplate(final RedisConnectionFactory connectionFactory) {
            return new StringRedisTemplate(connectionFactory);
        }

        @Bean
        public RateLimiter redisRateLimiter(@Qualifier("rateLimiterRedisTemplate") final RedisTemplate redisTemplate) {
            return new RedisRateLimiter(redisTemplate);
        }
    }

    @ConditionalOnConsulEnabled
    @ConditionalOnMissingBean(RateLimiter.class)
    @ConditionalOnProperty(prefix = RateLimitProperties.PREFIX, name = "repository", havingValue = "CONSUL")
    public static class ConsulConfiguration {

        @Bean
        public RateLimiter consultRateLimiter(final ConsulClient consulClient, final ObjectMapper objectMapper) {
            return new ConsulRateLimiter(consulClient, objectMapper);
        }

    }

    @EntityScan
    @EnableJpaRepositories
    @ConditionalOnMissingBean(RateLimiter.class)
    @ConditionalOnProperty(prefix = RateLimitProperties.PREFIX, name = "repository", havingValue = "JPA")
    public static class SpringDataConfiguration {

        @Bean
        public RateLimiter springDataRateLimiter(final RateLimiterRepository rateLimiterRepository) {
            return new JpaRateLimiter(rateLimiterRepository);
        }

    }

    @ConditionalOnMissingBean(RateLimiter.class)
    @ConditionalOnProperty(prefix = RateLimitProperties.PREFIX, name = "repository", havingValue = "IN_MEMORY", matchIfMissing = true)
    public static class InMemoryConfiguration {

        @Bean
        public RateLimiter inMemoryRateLimiter() {
            return new InMemoryRateLimiter();
        }
    }

}
