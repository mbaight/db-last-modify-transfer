package com.sgsl.foodsee.cloud.lastmodify;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by maoxianzhi.
 * CreateTime: 2017/10/30
 * ModifyBy  maoxianzhi
 * ModifyTime: 2017/10/30
 * Description:
 */

@Slf4j
public class CacheControlPreControllerZuulFilter extends ZuulFilter {


    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.ENGLISH);

    private static final LocalDateTime BEGIN_LASTMODIFY_DATE_TIME = LocalDateTime.of(1997, Month.JANUARY, 1, 1, 1, 1);


    private final LastModifyDataProvider lastModifyDataProvider;
    private final LastModifyMetaDataProvider lastModifyMetaDataProvider;


    @Data
    @Builder
    private static class TimesFromRequest {
        //数据最近修改时间
        @NonNull
        LocalDateTime ifModifySince;
        //数据过期时间
        @NonNull
        LocalDateTime expirseTime;
    }

    public CacheControlPreControllerZuulFilter(LastModifyDataProvider lastModifyDataProvider, LastModifyMetaDataProvider lastModifyMetaDataProvider) {
        this.lastModifyMetaDataProvider = lastModifyMetaDataProvider;
        this.lastModifyDataProvider = lastModifyDataProvider;
    }

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 100;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        return "GET".equalsIgnoreCase(request.getMethod());
    }

    @Override
    public Object run() {
        try {
            RequestContext context = RequestContext.getCurrentContext();
            HttpServletRequest request = context.getRequest();

            TimesFromRequest timesFromRequest = getTimesFromRequest(request);

            log.debug("获取到客户端cache信息：{}", timesFromRequest);

            //如果未过期，直接返回304
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(timesFromRequest.getExpirseTime())) {
                context.setSendZuulResponse(false);
                context.setResponseBody(HttpStatus.NOT_MODIFIED.getReasonPhrase());
                context.setResponseStatusCode(HttpStatus.NOT_MODIFIED.value());
                return null;
            }

            String requestURI = request.getRequestURI();
            LastModifyMetaDataProvider.LastModifyMetaDatas lastModifyMetaDatas = lastModifyMetaDataProvider.getLastModifyMetaDatas(requestURI);

            //如果当前URL未设置cache,则不做处理
            if (Objects.isNull(lastModifyMetaDatas)) {
                log.debug("pass this request on not last-modify");
                return null;
            }

            lastModifyMetaDatas.getLastModifyMetaDataWithService().setLastModifyMetaDataList(new ArrayList<>());
            log.debug("found lastModifyMetaDatas:{} with requestURI:{}", lastModifyMetaDatas, requestURI);

            LastModifyMetaDataWithService lastModifyMetaDataWithService = lastModifyMetaDatas.getLastModifyMetaDataWithService();
            LastModifyMetaData lastModifyMetaData = lastModifyMetaDatas.getLastModifyMetaData();

            String userId = "0";

            if (lastModifyMetaData.isUserData()) {
                userId = request.getHeader("user_id");
                Validate.notEmpty(userId);
            }

            String finalUserId = userId;


            List<String> tableNameKeys = lastModifyMetaData.getTableNames().stream().map(tableName -> String.format("%s_%s_%s_%s",
                    lastModifyMetaDataWithService.getServiceName(),
                    lastModifyMetaDataWithService.getDbName(),
                    tableName,
                    finalUserId
            )).collect(Collectors.toList());


            log.debug("found tableNameKeys:{} with requestURI:{}", tableNameKeys, requestURI);

            List<LastModifyData> lastModifyDataList = lastModifyDataProvider.findLastModifyDatas(tableNameKeys);

            log.debug("found lastModifyDatas:{} with requestURI:{}", lastModifyDataList, requestURI);

            if (CollectionUtils.isEmpty(lastModifyDataList)) {
                log.error("not found lastModifyDataList from mongodb: tableNameKeys:{}", tableNameKeys);
                return null;
            }

            Object[] tableModifyTimes = lastModifyDataList.stream().map(LastModifyData::getLastUpdateTime).toArray();
            long lastModifyTimeOnCompare = min(tableModifyTimes);
            long lastModifyTime = max(tableModifyTimes);

            log.debug("calculation lastModifyTimeOnCompare:{} with requestURI:{}", lastModifyTimeOnCompare, requestURI);

            LocalDateTime lastModifyDateTimeOnCompare = LocalDateTime.ofInstant(new Date(lastModifyTimeOnCompare).toInstant(), CacheControlConstant.zoneId);
            LocalDateTime lastModifyDateTime = LocalDateTime.ofInstant(new Date(lastModifyTime).toInstant(), CacheControlConstant.zoneId);
            String lastModifyTimeOnResponse = lastModifyDateTime.format(dateTimeFormatter);

            LocalDateTime ifModifySince = timesFromRequest.getIfModifySince();

            if (ifModifySince.equals(lastModifyDateTimeOnCompare) || ifModifySince.isAfter(lastModifyDateTimeOnCompare)) {
                log.debug("pass this request : lastModifyDateTimeOnCompare:{}, ifModifySince:{}", lastModifyDateTimeOnCompare, ifModifySince);

                context.setSendZuulResponse(false);
                context.setResponseBody(HttpStatus.NOT_MODIFIED.getReasonPhrase());
                context.setResponseStatusCode(HttpStatus.NOT_MODIFIED.value());

                long flushDataIntervalSecond = lastModifyMetaData.getFlushDataIntervalSecond();
                String cacheControl = String.format("%s%d,public", CacheControlConstant.MAX_AGE_TAG, flushDataIntervalSecond);
                String expirse = now.plusSeconds(flushDataIntervalSecond).format(dateTimeFormatter);

                context.addZuulResponseHeader(CacheControlConstant.CACHE_CONTROL_TAG, cacheControl);
                context.addZuulResponseHeader(CacheControlConstant.LASTMODIFY_TAG, lastModifyTimeOnResponse);
                context.addZuulResponseHeader(CacheControlConstant.EXPIRES_TAG, expirse);

                log.debug("set last-modify: cacheControl:{}, last-modify-time:{}, expires:{}",
                        cacheControl,
                        lastModifyTimeOnResponse,
                        expirse);
            } else {
                long flushDataIntervalSecond = lastModifyMetaData.getFlushDataIntervalSecond();
                String cacheControl = String.format("%s%d,public", CacheControlConstant.MAX_AGE_TAG, flushDataIntervalSecond);
                context.set(CacheControlConstant.CACHE_CONTROL_TAG, cacheControl);
                context.set(CacheControlConstant.LASTMODIFY_TAG, lastModifyTimeOnResponse);
                String expirse = now.plusSeconds(flushDataIntervalSecond).format(dateTimeFormatter);
                context.set(CacheControlConstant.EXPIRES_TAG, expirse);

                log.debug("set last-modify: cacheControl:{}, last-modify-time:{}, expires:{}",
                        cacheControl,
                        lastModifyTimeOnResponse,
                        expirse);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }


    private TimesFromRequest getTimesFromRequest(HttpServletRequest request) {
        String cacheControl = request.getHeader(CacheControlConstant.CACHE_CONTROL_TAG);
        if (StringUtils.isNotEmpty(cacheControl)) {
            String ifModifySince = request.getHeader(CacheControlConstant.IF_MODIFY_SINCE_TAG);
            if (StringUtils.isNotEmpty(ifModifySince)) {
                log.debug("found {}:{} from request:{}", CacheControlConstant.IF_MODIFY_SINCE_TAG, ifModifySince, request.getRequestURI());

                LocalDateTime lastModified;
                if (StringUtils.isNumeric(ifModifySince)) {
                    lastModified = LocalDateTime.ofInstant(new Date(Long.parseLong(ifModifySince)).toInstant(), CacheControlConstant.zoneId);
                } else {
                    lastModified = LocalDateTime.from(dateTimeFormatter.parse(ifModifySince));
                }

                long maxAge = parseMaxAge(cacheControl);

                if (maxAge < 0) {
                    log.error("不能获取 maxAge, cacheControl:{}, requestUrl:{}", cacheControl, request.getRequestURI());
                    maxAge = 0;
                }

                return TimesFromRequest.builder()
                        .ifModifySince(lastModified)
                        .expirseTime(lastModified.plusSeconds(maxAge))
                        .build();
            }
        }

        return TimesFromRequest.builder()
                .ifModifySince(BEGIN_LASTMODIFY_DATE_TIME)
                .expirseTime(BEGIN_LASTMODIFY_DATE_TIME)
                .build();
    }

    private long parseMaxAge(String cacheControl) {
        String[] cacheItems = cacheControl.split(",");
        for (String cacheItem : cacheItems) {
            if (cacheItem.startsWith(CacheControlConstant.MAX_AGE_TAG)) {
                return Long.parseLong(cacheControl.substring(CacheControlConstant.MAX_AGE_TAG.length()));
            }
        }

        return 0;
    }


    private Long min(Object[] array) {
        Validate.isTrue(array.length > 0);
        Long min = (Long) array[0];
        for (Object lastModifyTime : array) {
            if ((Long) lastModifyTime < min) {
                min = (Long) lastModifyTime;
            }
        }

        return min;
    }

    private Long max(Object[] array) {
        Validate.isTrue(array.length > 0);
        Long min = (Long) array[0];
        for (Object lastModifyTime : array) {
            if ((Long) lastModifyTime > min) {
                min = (Long) lastModifyTime;
            }
        }

        return min;
    }
}
