package com.sgsl.foodsee.cloud.lastmodify;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.PathMatcher;

import java.util.*;

/**
 * Created by maoxianzhi.
 * CreateTime: 2017/10/26
 * ModifyBy  maoxianzhi
 * ModifyTime: 2017/10/26
 * Description:
 */

@Slf4j
public class LastModifyMetaDataProvider {
    @Getter
    private final LastModifyMetaDataProperties lastModifyMetaDataProperties;

    private final Map<String, Map<String, LastModifyMetaData>> lastModifyMetaDataFromServiceNameMap = new HashMap<>();
    private final Map<String, Map<String, LastModifyMetaDataWithService>> lastModifyMetaDataFromServiceNameWithServiceMap = new HashMap<>();

    private final PathMatcher pathMatcher = new AntPathMatcher();


    public LastModifyMetaDataProvider(LastModifyMetaDataProperties lastModifyMetaDataProperties) throws LastModifyMetaDataException {
        this.lastModifyMetaDataProperties = lastModifyMetaDataProperties;
        List<LastModifyMetaDataWithService> lastModifyMetaData = lastModifyMetaDataProperties.getLastModifyMetaDataWithServices();
        if (CollectionUtils.isEmpty(lastModifyMetaData)) {
            throw new LastModifyMetaDataException("没有lastmodify的相关配置");
        }

        for (LastModifyMetaDataWithService lastModifyMetaDataWithService : lastModifyMetaData) {
            String serviceName = lastModifyMetaDataWithService.getServiceName();

            List<LastModifyMetaData> lastModifyMetaDataList = lastModifyMetaDataWithService.getLastModifyMetaDataList();

            Map<String, LastModifyMetaData> lastModifyMetaDataMap = new HashMap<>(lastModifyMetaDataList.size());
            Map<String, LastModifyMetaDataWithService> lastModifyMetaDataWithServiceMap = new HashMap<>(lastModifyMetaDataList.size());

            for (LastModifyMetaData modifyMetaData : lastModifyMetaDataList) {
                String apiName = modifyMetaData.getApiName();
                String serviceUri = apiName.startsWith("/") ?
                        String.format("/%s%s", serviceName, apiName):
                        String.format("/%s/%s", serviceName, apiName);
                lastModifyMetaDataMap.put(serviceUri, modifyMetaData);
                lastModifyMetaDataWithServiceMap.put(serviceUri, lastModifyMetaDataWithService);
            }

            lastModifyMetaDataFromServiceNameMap.put(serviceName, lastModifyMetaDataMap);
            lastModifyMetaDataFromServiceNameWithServiceMap.put(serviceName, lastModifyMetaDataWithServiceMap);
        }
    }


    public List<LastModifyMetaDataWithService> getLastModifyMetaDataWithServices() {
        return lastModifyMetaDataProperties.getLastModifyMetaDataWithServices();
    }

    @Data
    @Builder
    public static class LastModifyMetaDatas {
        @NonNull
        private final LastModifyMetaData lastModifyMetaData;
        @NonNull
        private final LastModifyMetaDataWithService lastModifyMetaDataWithService;
    }

    public LastModifyMetaDatas getLastModifyMetaDatas(String requestURI) {
        int splitterIndex = requestURI.indexOf('/', 1);
        String serviceName = requestURI.substring(1, splitterIndex);

        Map<String, LastModifyMetaData> modifyMetaDataMap = lastModifyMetaDataFromServiceNameMap.get(serviceName);
        Map<String, LastModifyMetaDataWithService> modifyDataWithServiceMap = lastModifyMetaDataFromServiceNameWithServiceMap.get(serviceName);
        if (CollectionUtils.isEmpty(modifyMetaDataMap) || CollectionUtils.isEmpty(modifyDataWithServiceMap)) {
            log.error("not found modfiy-map  from service:{} with url:{}", serviceName, requestURI);
            return null;
        }


        LastModifyMetaData lastModifyMetaData = modifyMetaDataMap.get(requestURI);
        LastModifyMetaDataWithService lastModifyMetaDataWithService = modifyDataWithServiceMap.get(requestURI);

        if (Objects.nonNull(lastModifyMetaData) && Objects.nonNull(lastModifyMetaDataWithService)) {
            return LastModifyMetaDatas.builder()
                    .lastModifyMetaData(lastModifyMetaData)
                    .lastModifyMetaDataWithService(lastModifyMetaDataWithService)
                    .build();
        }

        List<String> matchingPatterns = new ArrayList<>();
        for (String registeredPattern : modifyMetaDataMap.keySet()) {
            if (pathMatcher.match(registeredPattern, requestURI)) {
                matchingPatterns.add(registeredPattern);
            } else {
                if (!registeredPattern.endsWith("/") && pathMatcher.match(registeredPattern + "/", requestURI)) {
                    matchingPatterns.add(registeredPattern + "/");
                }
            }
        }

        String bestMatch = null;
        Comparator<String> patternComparator = pathMatcher.getPatternComparator(requestURI);
        if (!matchingPatterns.isEmpty()) {
            matchingPatterns.sort(patternComparator);
            log.debug("Matching patterns for request {} are {} ", requestURI, matchingPatterns);
            bestMatch = matchingPatterns.get(0);
        }

        if (Objects.nonNull(bestMatch)) {
            return LastModifyMetaDatas.builder()
                    .lastModifyMetaData(modifyMetaDataMap.get(bestMatch))
                    .lastModifyMetaDataWithService(modifyDataWithServiceMap.get(bestMatch))
                    .build();
        }

        return null;
    }
}
