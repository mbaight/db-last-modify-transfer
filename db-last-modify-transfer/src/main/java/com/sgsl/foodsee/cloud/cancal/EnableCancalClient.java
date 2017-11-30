package com.sgsl.foodsee.cloud.cancal;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Created by maoxianzhi.
 * CreateTime: 2017/10/25
 * ModifyBy  maoxianzhi
 * ModifyTime: 2017/10/25
 * Description:
 */

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(CancalClientAutoConfiguration.class)
public @interface EnableCancalClient {
}
