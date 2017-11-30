package com.sgsl.foodsee.cloud.ratelimit;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.kv.model.GetValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static org.springframework.util.StringUtils.hasText;

/**
 * Created by Administrator .
 * create_time: 2017/11/30 0030
 * modify_time: 2017/11/30 0030
 */

@Slf4j
@RequiredArgsConstructor
public class ConsulRateLimiter extends AbstractRateLimiter {

    private final ConsulClient consulClient;
    private final ObjectMapper objectMapper;

    @Override
    protected Rate getRate(String key) {
        Rate rate = null;
        GetValue value = this.consulClient.getKVValue(key).getValue();
        if (value != null && value.getDecodedValue() != null) {
            try {
                rate = this.objectMapper.readValue(value.getDecodedValue(), Rate.class);
            } catch (IOException e) {
                log.error("Failed to deserialize Rate", e);
            }
        }
        return rate;
    }

    @Override
    protected void saveRate(Rate rate) {
        String value = "";
        try {
            value = this.objectMapper.writeValueAsString(rate);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize Rate", e);
        }

        if (hasText(value)) {
            this.consulClient.setKVValue(rate.getKey(), value);
        }
    }

}
