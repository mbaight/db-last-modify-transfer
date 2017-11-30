package com.sgsl.foodsee.cloud.ratelimit;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator .
 * create_time: 2017/11/30 0030
 * modify_time: 2017/11/30 0030
 */

public class InMemoryRateLimiter extends AbstractRateLimiter {

    private Map<String, Rate> repository = new ConcurrentHashMap<>();

    @Override
    protected Rate getRate(String key) {
        return this.repository.get(key);
    }

    @Override
    protected void saveRate(Rate rate) {
        this.repository.put(rate.getKey(), rate);
    }

}
