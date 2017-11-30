package com.sgsl.foodsee.cloud.lastmodify;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author maoxianzhi on 17.10.14.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(LastModifyApiAutoConfiguration.class)
public @interface EnableLastModifyProvider {
}
