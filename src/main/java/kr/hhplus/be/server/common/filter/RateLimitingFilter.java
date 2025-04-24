package kr.hhplus.be.server.common.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Order(3)
@Slf4j
@Component
public class RateLimitingFilter implements Filter {

    private final Map<String, RequestTracker> requestCounts = new ConcurrentHashMap<>();
    private final int limit;
    private final long windowMs;

    public RateLimitingFilter() {
        this(10000, 60 * 1000); // 기본값: 1분, 5회
    }

    // 테스트 용 생성자
    public RateLimitingFilter(int limit, long windowMs) {
        this.limit = limit;
        this.windowMs = windowMs;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String ip = httpRequest.getRemoteAddr();
        long now = Instant.now().toEpochMilli();

        RequestTracker tracker = requestCounts.computeIfAbsent(ip, k -> new RequestTracker());
        synchronized (tracker) {
            if (now - tracker.startTime > windowMs) {
                tracker.reset(now);
            }

            if (tracker.count >= limit) {
                log.warn("Rate limit exceeded for IP: {}", ip);
                throw new ServletException("Rate limit exceeded. Try again later.");
            }

            tracker.count++;
        }

        chain.doFilter(request, response);
    }

    private static class RequestTracker {
        long startTime = Instant.now().toEpochMilli();
        int count = 0;

        void reset(long now) {
            this.startTime = now;
            this.count = 0;
        }
    }
}
