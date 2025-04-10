package kr.hhplus.be.server.domain.order.service.dto;

import kr.hhplus.be.server.domain.order.dto.GetCartServiceRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GetCartServiceRequestTest {

    @Test
    @DisplayName("성공: 유효한 userId로 객체 생성")
    void create_success() {
        // given
        Long userId = 1L;

        // when
        GetCartServiceRequest request = new GetCartServiceRequest(userId);

        // then
        assertNotNull(request);
        assertEquals(1L, request.userId());
    }

    @Test
    @DisplayName("실패: userId가 null이면 예외 발생")
    void create_fail_userId_null() {
        // expect
        assertThrows(IllegalArgumentException.class, () -> new GetCartServiceRequest(null));
    }

    @Test
    @DisplayName("실패: userId가 1 미만이면 예외 발생")
    void create_fail_userId_less_than_1() {
        // expect
        assertThrows(IllegalArgumentException.class, () -> new GetCartServiceRequest(0L));
    }
}

