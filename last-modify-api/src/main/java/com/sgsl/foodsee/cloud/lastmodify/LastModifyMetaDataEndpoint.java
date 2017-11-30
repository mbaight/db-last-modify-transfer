package com.sgsl.foodsee.cloud.lastmodify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.AbstractEndpoint;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;

import java.util.List;

/**
 * @author maoxianzhi on 10/19
 */

@Slf4j
@ConfigurationProperties(prefix = LastModifyMetaDataProperties.PROPERTIES_PREFIX)
public class LastModifyMetaDataEndpoint extends AbstractEndpoint<LastModifyMetaDataWrapper> {
    private final LastModifyMetaDataProvider lastModifyMetaDataProvider;
    private final String applicationName;

    public LastModifyMetaDataEndpoint(LastModifyMetaDataProvider lastModifyMetaDataProvider, Environment environment) {
        super("last_modify_meta_data");
        this.lastModifyMetaDataProvider = lastModifyMetaDataProvider;
        this.applicationName = environment.getProperty("spring.application.name");
        log.debug("LastModifyMetaDataEndpoint statted");
    }

    @Override
    public LastModifyMetaDataWrapper invoke() {
        List<LastModifyMetaDataWithService> lastModifyMetaDatas = lastModifyMetaDataProvider.getLastModifyMetaDataWithServices();

        return LastModifyMetaDataWrapper.builder()
                .errorMessage("ok")
                .lastModifyMetaData(lastModifyMetaDatas)
                .serviceName(applicationName)
                .build();
    }
}
