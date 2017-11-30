package com.sgsl.foodsee.cloud.ratelimit;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Created by Administrator .
 * create_time: 2017/11/30 0030
 * modify_time: 2017/11/30 0030
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rate {

    private String key;
    private Long remaining;
    private Long remainingQuota;
    private Long reset;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private Date expiration;

}
