package com.sgsl.foodsee.cloud.cancal;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Created by maoxianzhi.
 * CreateTime: 2017/10/25
 * ModifyBy  maoxianzhi
 * ModifyTime: 2017/10/25
 * Description:
 */

@EqualsAndHashCode(callSuper = false)
@Data
public class CanalProcessException extends RuntimeException {
    private final Exception exception;

    public CanalProcessException(String s, Exception e) {
        super(s);
        this.exception = e;
    }
}
