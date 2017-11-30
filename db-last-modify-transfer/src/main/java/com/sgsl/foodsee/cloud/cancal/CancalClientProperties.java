package com.sgsl.foodsee.cloud.cancal;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.Validate;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Created by maoxianzhi.
 * CreateTime: 2017/10/24
 * ModifyBy  maoxianzhi
 * ModifyTime: 2017/10/24
 * Description:
 */

@Slf4j
@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = CancalClientProperties.PROPERTIES_PREFIX)
public class CancalClientProperties {
    public static final String PROPERTIES_PREFIX = "sgsl.custom.canal.config";

    private boolean cluster = false;

    private SimpleProperties simpleProperties;

    private ClusterProperties clusterProperties;

    @NonNull
    private String dstination;

    @NonNull
    private List<String> userIdColumnNames;


    private int batchSize = 1024;

    @NonNull
    private List<DBSubjection> dbSubjections;

    @Data
    @NoArgsConstructor
    public static class SimpleProperties {

        @NonNull
        private String canalServerIp;
        @NonNull
        private Integer canalServerPort;

        private String userName = "";
        private String password = "";
    }


    @Data
    @NoArgsConstructor
    public static class ClusterProperties {

        @NonNull
        private String zkAddress;

        private String userName = "";
        private String password = "";
    }

    @Data
    @NoArgsConstructor
    public static class DBSubjection {

        /**
         * 当前数据库名称
         */
        @NonNull
        private String dbName;

        /**
         * 当前数据库的微服务名称
         */
        @NonNull
        private String serviceName;
    }


    public String findServiceNameFromDbName(String dbName) {
        Validate.notEmpty(dbName);
        for (DBSubjection dbSubjection : dbSubjections) {
            if (dbSubjection.getDbName().equalsIgnoreCase(dbName)) {
                return dbSubjection.getServiceName();
            }
        }

        return null;
    }

}
