package com.sgsl.foodsee.cloud.ratelimit;

/**
 * Created by Administrator .
 * create_time: 2017/11/30 0030
 * modify_time: 2017/11/30 0030
 */

import org.springframework.cloud.netflix.zuul.filters.Route;

import javax.servlet.http.HttpServletRequest;

/**
 * Key generator for rate limit control.
 */
public interface RateLimitKeyGenerator {

    /**
     * Returns a key based on {@link HttpServletRequest}, {@link Route} and
     * {@link Policy}
     *
     * @param request The {@link HttpServletRequest}
     * @param route   The {@link Route}
     * @param policy  The
     * {@link Policy}
     * @return Generated key
     */
    String key(HttpServletRequest request, Route route, Policy policy);
}
