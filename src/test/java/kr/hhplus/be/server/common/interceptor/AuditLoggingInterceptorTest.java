package kr.hhplus.be.server.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditLoggingInterceptorTest {

    private AuditLoggingInterceptor auditLoggingInterceptor;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        auditLoggingInterceptor = new AuditLoggingInterceptor();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
    }

    @Test
    @DisplayName("성공: 모든 요청에 대해 감사 로그 실행됨")
    void auditLog_preHandle_allRequests() throws Exception {
        // given
        when(request.getRequestURI()).thenReturn("/v1/products");
        when(request.getMethod()).thenReturn("GET");
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        // when
        boolean result = auditLoggingInterceptor.preHandle(request, response, new Object());

        // then
        assertThat(result).isTrue(); // 요청은 계속 흐름 타야 하니까 true
        // 로그 검증 대신 흐름 통과 여부 확인
    }
}
