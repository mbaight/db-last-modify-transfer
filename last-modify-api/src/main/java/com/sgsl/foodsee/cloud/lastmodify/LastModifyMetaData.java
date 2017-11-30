package com.sgsl.foodsee.cloud.lastmodify;

import lombok.*;

import java.util.List;

/**
 * Created by maoxianzhi.
 * CreateTime: 2017/10/26
 * ModifyBy  maoxianzhi
 * ModifyTime: 2017/10/26
 * Description:
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LastModifyMetaData {
    /**
     * 接口名称
     */
    @NonNull
    private String apiName;


    /**
     * 是否用户数据
     */
    private boolean userData;


    /**
     * 需要访问到的表名称列表
     */
    @NonNull
    private List<String> tableNames;

    /**
     * 刷新缓存间隔
     */
    private long flushDataIntervalSecond = 300;
}
