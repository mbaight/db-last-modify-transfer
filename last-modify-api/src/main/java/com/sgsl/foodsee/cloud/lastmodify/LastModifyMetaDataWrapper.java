package com.sgsl.foodsee.cloud.lastmodify;

import lombok.*;

import java.util.List;

/**
 * Created by maoxianzhi.
 * CreateTime: 2017/10/27
 * ModifyBy  maoxianzhi
 * ModifyTime: 2017/10/27
 * Description:
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LastModifyMetaDataWrapper {
    private String errorMessage = "ok";

    private String serviceName;

    @NonNull
    private List<LastModifyMetaDataWithService> lastModifyMetaData;
}
