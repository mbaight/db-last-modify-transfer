package com.sgsl.foodsee.cloud.ratelimit;

import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.util.ZuulRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.util.UrlPathHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.FORM_BODY_WRAPPER_FILTER_ORDER;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

/**
 * Created by Administrator .
 * create_time: 2017/11/30 0030
 * modify_time: 2017/11/30 0030
 */
public class RateLimitPreFilter extends AbstractRateLimitFilter {

    private final RateLimiter rateLimiter;
    private final RateLimitKeyGenerator rateLimitKeyGenerator;

    public RateLimitPreFilter(final RateLimitProperties properties, final RouteLocator routeLocator,
                              final UrlPathHelper urlPathHelper, final RateLimiter rateLimiter,
                              final RateLimitKeyGenerator rateLimitKeyGenerator) {
        super(properties, routeLocator, urlPathHelper);
        this.rateLimiter = rateLimiter;
        this.rateLimitKeyGenerator = rateLimitKeyGenerator;
    }

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FORM_BODY_WRAPPER_FILTER_ORDER;
    }

    @Override
    public Object run() {
        final RequestContext ctx = RequestContext.getCurrentContext();
        final HttpServletResponse response = ctx.getResponse();
        final HttpServletRequest request = ctx.getRequest();
        final Route route = route();

        policy(route).ifPresent(policy -> {
            final String key = rateLimitKeyGenerator.key(request, route, policy);
            final Rate rate = rateLimiter.consume(policy, key, null);

            final Long limit = policy.getLimit();
            final Long remaining = rate.getRemaining();
            if (limit != null) {
                response.setHeader(RateLimitConstants.LIMIT_HEADER, String.valueOf(limit));
                response.setHeader(RateLimitConstants.REMAINING_HEADER, String.valueOf(Math.max(remaining, 0)));
            }

            final Long quota = policy.getQuota();
            final Long remainingQuota = rate.getRemainingQuota();
            if (quota != null) {
                RequestContextHolder.getRequestAttributes()
                        .setAttribute(RateLimitConstants.REQUEST_START_TIME, System.currentTimeMillis(), SCOPE_REQUEST);
                response.setHeader(RateLimitConstants.QUOTA_HEADER, String.valueOf(quota));
                response.setHeader(RateLimitConstants.REMAINING_QUOTA_HEADER,
                        String.valueOf(MILLISECONDS.toSeconds(Math.max(remainingQuota, 0))));
            }

            response.setHeader(RateLimitConstants.RESET_HEADER, String.valueOf(rate.getReset()));

            if ((limit != null && remaining < 0) || (quota != null && remainingQuota < 0)) {
                HttpStatus tooManyRequests = HttpStatus.TOO_MANY_REQUESTS;
                ctx.setResponseStatusCode(tooManyRequests.value());
                ctx.put("rateLimitExceeded", "true");
                ctx.setSendZuulResponse(false);
                ZuulException zuulException = new ZuulException(tooManyRequests.toString(), tooManyRequests.value(),
                        null);
                throw new ZuulRuntimeException(zuulException);
            }
        });

        return null;
    }
}
