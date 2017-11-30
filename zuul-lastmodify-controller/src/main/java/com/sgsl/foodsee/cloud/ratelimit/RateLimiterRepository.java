package com.sgsl.foodsee.cloud.ratelimit;

import org.springframework.data.repository.CrudRepository;

/**
 * Created by Administrator .
 * create_time: 2017/11/30 0030
 * modify_time: 2017/11/30 0030
 */

public interface RateLimiterRepository extends CrudRepository<Rate, String> {

}
