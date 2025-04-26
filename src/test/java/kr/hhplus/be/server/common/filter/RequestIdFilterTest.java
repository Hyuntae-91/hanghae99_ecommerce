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

class RequestIdFilterTest {

    private RequestIdFilter requestIdFilter;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        requestIdFilter = new RequestIdFilter();
        chain = mock(FilterChain.class);
    }

    @Test
    @DisplayName("성공: X-Request-ID 헤더가 없을 경우 UUID 생성")
    void requestIdGeneratedWhenMissing() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        requestIdFilter.doFilter(request, response, chain);

        // then
        String requestId = (String) request.getAttribute(RequestIdFilter.HEADER_NAME);
        assertThat(requestId).isNotBlank();
        assertThat(requestId).hasSizeGreaterThan(10); // UUID 예상
        verify(chain, times(1)).doFilter(any(), eq(response));
    }

    @Test
    @DisplayName("성공: X-Request-ID 헤더가 있을 경우 유지")
    void requestIdPreservedWhenPresent() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(RequestIdFilter.HEADER_NAME, "test-request-id");

        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        requestIdFilter.doFilter(request, response, chain);

        // then
        assertThat(request.getAttribute(RequestIdFilter.HEADER_NAME)).isEqualTo("test-request-id");
        verify(chain, times(1)).doFilter(any(), eq(response));
    }
}
