package com.sgsl.foodsee.cloud.lastmodify;

import java.time.ZoneId;

/**
 * Created by Administrator .
 * create_time: 2017/11/16 0016
 * modify_time: 2017/11/16 0016
 */
public interface CacheControlConstant {
    String CACHE_CONTROL_TAG = "Cache-Control";
    String LASTMODIFY_TAG = "Last-Modified";
    String EXPIRES_TAG = "expires";

    String IF_MODIFY_SINCE_TAG = "If-Modified-Since";

    String MAX_AGE_TAG = "max-age=";
    String ETAG_TAG = "ETag";
    ZoneId zoneId = ZoneId.of("Asia/Shanghai");

     String IF_NONE_MATCH_TAG = "If-None-Match";
}
