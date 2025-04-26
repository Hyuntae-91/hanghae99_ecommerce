package kr.hhplus.be.server.common.filter;

import jakarta.servlet.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;

@Order(1)
@Slf4j
@Component
public class IpBlockFilter implements Filter {

    private static final Set<String> BLOCKED_IPS = Set.of("10.10.10.10");

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String ip = request.getRemoteAddr();
        if (BLOCKED_IPS.contains(ip)) {
            log.warn("[Ip Filter] 차단된 IP의 접근 시도: {}", ip);
            throw new ServletException("Access denied from blocked IP: " + ip);
        }

        chain.doFilter(request, response);
    }
}
