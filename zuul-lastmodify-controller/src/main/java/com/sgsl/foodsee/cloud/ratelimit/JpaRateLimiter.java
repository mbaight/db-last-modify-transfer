package com.sgsl.foodsee.cloud.ratelimit;

import lombok.RequiredArgsConstructor;

/**
 * Created by Administrator .
 * create_time: 2017/11/30 0030
 * modify_time: 2017/11/30 0030
 */

@RequiredArgsConstructor
public class JpaRateLimiter extends AbstractRateLimiter {

    private final RateLimiterRepository repository;

    @Override
    protected Rate getRate(String key) {
        return this.repository.findOne(key);
    }

    @Override
    protected void saveRate(Rate rate) {
        this.repository.save(rate);
    }

}
