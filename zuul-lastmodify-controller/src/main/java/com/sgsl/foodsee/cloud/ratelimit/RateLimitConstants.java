package com.sgsl.foodsee.cloud.ratelimit;

/**
 * Created by Administrator .
 * create_time: 2017/11/30 0030
 * modify_time: 2017/11/30 0030
 */
public class RateLimitConstants {

    public static final String QUOTA_HEADER = "X-RateLimit-Quota";
    public static final String REMAINING_QUOTA_HEADER = "X-RateLimit-Remaining-Quota";
    public static final String LIMIT_HEADER = "X-RateLimit-Limit";
    public static final String REMAINING_HEADER = "X-RateLimit-Remaining";
    public static final String RESET_HEADER = "X-RateLimit-Reset";
    public static final String REQUEST_START_TIME = "rateLimitRequestStartTime";

}
