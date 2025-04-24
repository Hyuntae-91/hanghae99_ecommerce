package kr.hhplus.be.server.config;

import kr.hhplus.be.server.common.interceptor.ApiLoggingInterceptor;
import kr.hhplus.be.server.common.interceptor.TimingInterceptor;
import kr.hhplus.be.server.common.interceptor.AuditLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final ApiLoggingInterceptor apiLoggingInterceptor;
    private final TimingInterceptor timingInterceptor;
    private final AuditLoggingInterceptor auditLoggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiLoggingInterceptor)
                .addPathPatterns("/**");

        registry.addInterceptor(timingInterceptor)
                .addPathPatterns("/**");

        registry.addInterceptor(auditLoggingInterceptor)
                .addPathPatterns("/**");
    }
}
