package kr.hhplus.be.server.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class TimingInterceptorTest {

    private TimingInterceptor timingInterceptor;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        timingInterceptor = new TimingInterceptor();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
    }

    @Test
    @DisplayName("성공: preHandle에서 시작 시간 저장")
    void preHandle_setsStartTime() {
        // given
        when(request.getRequestURI()).thenReturn("/v1/products");

        // when
        boolean result = timingInterceptor.preHandle(request, response, new Object());

        // then
        assertThat(result).isTrue();
        verify(request).setAttribute(eq("startTime"), anyLong());
    }

    @Test
    @DisplayName("성공: afterCompletion에서 처리 시간 로그")
    void afterCompletion_logsDuration() {
        // given
        long fakeStartTime = System.currentTimeMillis() - 100;
        when(request.getAttribute("startTime")).thenReturn(fakeStartTime);
        when(request.getRequestURI()).thenReturn("/v1/products");
        when(request.getMethod()).thenReturn("GET");

        // when
        timingInterceptor.afterCompletion(request, response, new Object(), null);

        // then
        assertThatCode(() -> timingInterceptor.afterCompletion(request, response, new Object(), null))
                .doesNotThrowAnyException();
    }
}
