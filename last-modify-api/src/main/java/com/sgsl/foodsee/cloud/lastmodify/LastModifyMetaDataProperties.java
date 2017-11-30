package com.sgsl.foodsee.cloud.lastmodify;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * @author liuyo on 17.10.13.
 */
@Data
@ConfigurationProperties(prefix = LastModifyMetaDataProperties.PROPERTIES_PREFIX)
@NoArgsConstructor
public class LastModifyMetaDataProperties {
    public static final String PROPERTIES_PREFIX = "sgsl.custom.lastmodify.metadata.config";

    @NonNull
    private List<LastModifyMetaDataWithService> lastModifyMetaDataWithServices;
}
