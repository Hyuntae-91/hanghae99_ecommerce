package kr.hhplus.be.server.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;

import java.io.IOException;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RateLimitingFilterTest {

    private RateLimitingFilter rateLimitingFilter;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        rateLimitingFilter = new RateLimitingFilter();
        chain = mock(FilterChain.class);
    }

    @Test
    @DisplayName("성공: 요청 제한 이하일 경우 정상 처리")
    void underLimit_success() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("127.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        for (int i = 0; i < 5; i++) {
            rateLimitingFilter.doFilter(request, response, chain);
        }

        // then
        verify(chain, times(5)).doFilter(any(), any());
    }

    @Test
    @DisplayName("실패: 요청 제한 초과시 예외 발생")
    void overLimit_throwsException() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        IntStream.range(0, 5).forEach(i -> {
            try {
                rateLimitingFilter.doFilter(request, response, chain);
            } catch (Exception e) {
                fail("예외 발생하면 안됨: " + e.getMessage());
            }
        });

        // then: 6번째 요청에서 실패
        assertThatThrownBy(() -> rateLimitingFilter.doFilter(request, response, chain))
                .isInstanceOf(ServletException.class)
                .hasMessageContaining("Rate limit exceeded");
    }

    @Test
    @DisplayName("성공: 짧은 시간 윈도우 이후 요청 허용")
    void windowReset_allowsAgain() throws Exception {
        // limit=5, window=200ms
        rateLimitingFilter = new RateLimitingFilter(5, 200);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        for (int i = 0; i < 5; i++) {
            rateLimitingFilter.doFilter(request, response, chain);
        }

        Thread.sleep(250); // 제한시간 초과 시뮬레이션

        rateLimitingFilter.doFilter(request, response, chain);
        verify(chain, times(6)).doFilter(any(), any());
    }

}
