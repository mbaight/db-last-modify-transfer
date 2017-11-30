package com.sgsl.foodsee.cloud;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by maoxianzhi.
 * CreateTime: 2017/10/30
 * ModifyBy  maoxianzhi
 * ModifyTime: 2017/10/30
 * Description:
 */

@Slf4j
public class CacheControlPostControllerZuulFilter extends ZuulFilter {
    @Override
    public String filterType() {
        return FilterConstants.POST_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.SIMPLE_HOST_ROUTING_FILTER_ORDER + 1;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return false;
        }

        String cacheControl = (String) ctx.get(CacheControlConstant.CACHE_CONTROL_TAG);
        return StringUtils.isNotEmpty(cacheControl);
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();

        String cacheControl = (String) ctx.get(CacheControlConstant.CACHE_CONTROL_TAG);

        String lastModify = (String) ctx.get(CacheControlConstant.LASTMODIFY_TAG);
        String expires = (String) ctx.get(CacheControlConstant.EXPIRES_TAG);

        Validate.notEmpty(cacheControl);
        Validate.notEmpty(lastModify);
        Validate.notEmpty(expires);

        ctx.addZuulResponseHeader(CacheControlConstant.CACHE_CONTROL_TAG, cacheControl);
        ctx.addZuulResponseHeader(CacheControlConstant.LASTMODIFY_TAG, lastModify);
        ctx.addZuulResponseHeader(CacheControlConstant.EXPIRES_TAG, expires);

        log.debug("set cache on post response: cacheControl:{}, lastModify:{}, expires:{}",
                cacheControl,
                lastModify,
                expires);

        return null;
    }
}
