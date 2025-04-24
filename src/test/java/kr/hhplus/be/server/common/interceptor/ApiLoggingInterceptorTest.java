package kr.hhplus.be.server.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class ApiLoggingInterceptorTest {

    private ApiLoggingInterceptor apiLoggingInterceptor;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        apiLoggingInterceptor = new ApiLoggingInterceptor();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
    }

    @Test
    @DisplayName("성공: preHandle에서 요청 로그 흐름 확인")
    void preHandle_logsRequest() {
        // given
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/v1/products");

        // when
        boolean result = apiLoggingInterceptor.preHandle(request, response, new Object());

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("성공: afterCompletion에서 응답 상태코드 로깅")
    void afterCompletion_logsResponseStatus() {
        // given
        when(request.getRequestURI()).thenReturn("/v1/products");
        when(response.getStatus()).thenReturn(200);

        // when & then
        assertThatCode(() -> apiLoggingInterceptor.afterCompletion(request, response, new Object(), null))
                .doesNotThrowAnyException();
    }
}
