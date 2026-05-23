package com.portfolio.frauddetection.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(1)
@Slf4j
public class RateLimitFilter implements Filter {

    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private static final long WINDOW_MS = 60_000;

    private final Map<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String clientIp = getClientIp(httpRequest);
        String path = httpRequest.getRequestURI();

        if (path.startsWith("/actuator") || path.startsWith("/swagger") || path.startsWith("/api-docs")) {
            chain.doFilter(request, response);
            return;
        }

        RateLimitBucket bucket = buckets.computeIfAbsent(clientIp, k -> new RateLimitBucket());

        if (bucket.isRateLimited()) {
            log.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path);
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(429);
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Rate limit exceeded. Try again later.\",\"status\":429}");
            return;
        }

        bucket.increment();
        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class RateLimitBucket {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long windowStart = System.currentTimeMillis();

        boolean isRateLimited() {
            resetIfExpired();
            return count.get() >= MAX_REQUESTS_PER_MINUTE;
        }

        void increment() {
            resetIfExpired();
            count.incrementAndGet();
        }

        private void resetIfExpired() {
            long now = System.currentTimeMillis();
            if (now - windowStart > WINDOW_MS) {
                synchronized (this) {
                    if (now - windowStart > WINDOW_MS) {
                        count.set(0);
                        windowStart = now;
                    }
                }
            }
        }
    }
}
