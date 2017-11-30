package com.sgsl.foodsee.cloud.ratelimit;

/**
 * Created by Administrator .
 * create_time: 2017/11/30 0030
 * modify_time: 2017/11/30 0030
 */
public interface RateLimiter {
    /**
     * @param policy      Template for which rates should be created in case there's no rate limit associated with the
     *                    key
     * @param key         Unique key that identifies a request
     * @param requestTime The total time it took to handle the request
     * @return a view of a user's rate request limit
     */
    Rate consume(Policy policy, String key, Long requestTime);

}
