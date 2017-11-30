package com.sgsl.foodsee.cloud.cancal;


import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Created by maoxianzhi.
 * CreateTime: 2017/10/26
 * ModifyBy  maoxianzhi
 * ModifyTime: 2017/10/26
 * Description:
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection="DbConnectorHistory")
public class DbConnectorHistory {
    public static final String INDEX_KEY = "dbKey";
    public static final String LASTBATCHID_KEY = "lastBatchId";

    @NonNull
    @Indexed(unique = true)
    private String dbKey;

    @NonNull
    private Long lastBatchId;
}
