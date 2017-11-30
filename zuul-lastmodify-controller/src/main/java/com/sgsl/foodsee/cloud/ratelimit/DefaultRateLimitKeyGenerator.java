package com.sgsl.foodsee.cloud.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.netflix.zuul.filters.Route;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.StringJoiner;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.X_FORWARDED_FOR_HEADER;

/**
 * Created by Administrator .
 * create_time: 2017/11/30 0030
 * modify_time: 2017/11/30 0030
 */

@RequiredArgsConstructor
public class DefaultRateLimitKeyGenerator implements RateLimitKeyGenerator {

    private static final String ANONYMOUS_USER = "anonymous";

    private final RateLimitProperties properties;

    @Override
    public String key(final HttpServletRequest request, final Route route, final Policy policy) {
        final List<Policy.Type> types = policy.getType();
        final StringJoiner joiner = new StringJoiner(":");
        joiner.add(properties.getKeyPrefix());
        if (route != null) {
            joiner.add(route.getId());
        }
        if (!types.isEmpty()) {
            if (types.contains(Policy.Type.URL) && route != null) {
                joiner.add(route.getPath());
            }
            if (types.contains(Policy.Type.ORIGIN)) {
                joiner.add(getRemoteAddr(request));
            }
            if (types.contains(Policy.Type.USER)) {
                joiner.add(request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : ANONYMOUS_USER);
            }
        }
        return joiner.toString();
    }

    private String getRemoteAddr(final HttpServletRequest request) {
        String xForwardedFor = request.getHeader(X_FORWARDED_FOR_HEADER);
        if (properties.isBehindProxy() && xForwardedFor != null) {
            return xForwardedFor;
        }
        return request.getRemoteAddr();
    }
}
