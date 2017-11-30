package com.sgsl.foodsee.cloud.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Created by Administrator .
 * create_time: 2017/11/30 0030
 * modify_time: 2017/11/30 0030
 */

@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class RedisRateLimiter implements RateLimiter {

    private static final String QUOTA_SUFFIX = "-quota";

    private final RedisTemplate redisTemplate;

    @Override
    public Rate consume(final Policy policy, final String key, final Long requestTime) {
        final Long refreshInterval = policy.getRefreshInterval();
        final Long quota = policy.getQuota() != null ? SECONDS.toMillis(policy.getQuota()) : null;
        final Rate rate = new Rate(key, policy.getLimit(), quota, null, null);

        calcRemainingLimit(policy.getLimit(), refreshInterval, requestTime, key, rate);
        calcRemainingQuota(quota, refreshInterval, requestTime, key, rate);

        return rate;
    }

    private void calcRemainingLimit(Long limit, Long refreshInterval,
                                    Long requestTime, String key, Rate rate) {
        if (limit != null) {
            handleExpiration(key, refreshInterval, rate);
            long usage = requestTime == null ? 1L : 0L;
            Long current = this.redisTemplate.boundValueOps(key).increment(usage);
            rate.setRemaining(Math.max(-1, limit - current));
        }
    }

    private void calcRemainingQuota(Long quota, Long refreshInterval,
                                    Long requestTime, String key, Rate rate) {
        if (quota != null) {
            String quotaKey = key + QUOTA_SUFFIX;
            handleExpiration(quotaKey, refreshInterval, rate);
            Long usage = requestTime != null ? requestTime : 0L;
            Long current = this.redisTemplate.boundValueOps(quotaKey).increment(usage);
            rate.setRemainingQuota(Math.max(-1, quota - current));
        }
    }

    private void handleExpiration(String key, Long refreshInterval, Rate rate) {
        Long expire = this.redisTemplate.getExpire(key);
        if (expire == null || expire == -1) {
            this.redisTemplate.expire(key, refreshInterval, SECONDS);
            expire = refreshInterval;
        }
        rate.setReset(SECONDS.toMillis(expire));
    }
}
