package kr.hhplus.be.server.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;

import java.io.IOException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class IpBlockFilterTest {

    private IpBlockFilter ipBlockFilter;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        ipBlockFilter = new IpBlockFilter();
        chain = mock(FilterChain.class);
    }

    @Test
    @DisplayName("실패: 차단된 IP는 접근 불가 예외 발생")
    void blockedIp_shouldThrowException() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.10.10.10"); // 차단된 IP
        MockHttpServletResponse response = new MockHttpServletResponse();

        // then
        assertThatThrownBy(() -> ipBlockFilter.doFilter(request, response, chain))
                .isInstanceOf(ServletException.class)
                .hasMessageContaining("Access denied");
    }

    @Test
    @DisplayName("성공: 허용된 IP는 필터 통과")
    void allowedIp_shouldPass() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.168.1.1"); // 허용된 IP
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        ipBlockFilter.doFilter(request, response, chain);

        // then
        verify(chain, times(1)).doFilter(any(), eq(response));
    }
}
