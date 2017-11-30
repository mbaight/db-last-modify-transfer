package com.sgsl.foodsee.cloud.ratelimit;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Created by Administrator .
 * create_time: 2017/11/30 0030
 * modify_time: 2017/11/30 0030
 */

@Data
@Validated
@NoArgsConstructor
@ConfigurationProperties(RateLimitProperties.PREFIX)
public class RateLimitProperties {

    public static final String PREFIX = "zuul.ratelimit";

    private Policy defaultPolicy;

    @NotNull
    private Map<String, Policy> policies = Maps.newHashMap();

    private boolean behindProxy;

    private boolean enabled;

    @NotNull
    @Value("${spring.application.name:rate-limit-application}")
    private String keyPrefix;

    @NotNull
    private Repository repository = Repository.IN_MEMORY;

    public enum Repository {
        REDIS, CONSUL, JPA, IN_MEMORY
    }

    public Optional<Policy> getPolicy(String key) {
        return Optional.ofNullable(policies.getOrDefault(key, defaultPolicy));
    }

}