package com.sgsl.foodsee.cloud.ratelimit;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.web.util.UrlPathHelper;

import java.util.Optional;

/**
 * Created by Administrator .
 * create_time: 2017/11/30 0030
 * modify_time: 2017/11/30 0030
 */

@RequiredArgsConstructor
public abstract class AbstractRateLimitFilter extends ZuulFilter {

    private final RateLimitProperties properties;
    private final RouteLocator routeLocator;
    private final UrlPathHelper urlPathHelper;

    @Override
    public boolean shouldFilter() {
        return properties.isEnabled() && policy(route()).isPresent();
    }

    protected Route route() {
        String requestURI = urlPathHelper.getPathWithinApplication(RequestContext.getCurrentContext().getRequest());
        return routeLocator.getMatchingRoute(requestURI);
    }

    protected Optional<Policy> policy(final Route route) {
        if (route != null) {
            return properties.getPolicy(route.getId());
        }
        return Optional.ofNullable(properties.getDefaultPolicy());
    }
}
