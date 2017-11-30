package com.sgsl.foodsee.cloud.cancal;

import lombok.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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
@Document(collection = "last_modify_data")
public class LastModifyData {
    @Id
    private String id;

    /**
     * 当前数据库的微服务名称
     */
    @NonNull
    private String serviceName;

    /**
     * 当前数据库名称
     */
    @NonNull
    private String dbName;

    /**
     * 当前表名称
     */
    @NonNull
    private String tableName;

    /**
     * 更新数据的用户ID，如果是用户无关表（如商品表），则用户ID为0
     */
    @NonNull
    private Long userId = 0L;

    public String getId() {
        if (StringUtils.isNotEmpty(id)) {
            return id;
        }

        return id = String.format("%s_%s_%s_%d",
                serviceName,
                dbName,
                tableName,
                userId
        );
    }

    /**
     * 最近更新时间戳
     */
    @NonNull
    private Long lastUpdateTime;
}
