package com.sgsl.foodsee.cloud.lastmodify;

import lombok.*;

import java.util.List;

/**
 * Created by maoxianzhi.
 * CreateTime: 2017/10/31
 * ModifyBy  maoxianzhi
 * ModifyTime: 2017/10/31
 * Description:
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LastModifyMetaDataWithService {
    //服务名称
    @NonNull
    private String serviceName;
    //数据库名称
    @NonNull
    private String dbName;

    //数据库修改相关信息元数据
    @NonNull
    private List<LastModifyMetaData> lastModifyMetaDataList;
}
