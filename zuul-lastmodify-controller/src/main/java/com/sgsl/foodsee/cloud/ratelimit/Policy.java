package com.sgsl.foodsee.cloud.ratelimit;

import lombok.Data;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * Created by Administrator .
 * create_time: 2017/11/30 0030
 * modify_time: 2017/11/30 0030
 */

@Data
public class Policy {

    @NonNull
    private Long refreshInterval = MINUTES.toSeconds(1L);

    private Long limit;

    private Long quota;

    @NonNull
    private List<Type> type = new ArrayList<Type>();

    public enum Type {
        ORIGIN, USER, URL
    }
}
