package kr.hhplus.be.server.api.controller.coupon;

import kr.hhplus.be.server.domain.coupon.model.Coupon;
import kr.hhplus.be.server.domain.coupon.model.CouponType;
import kr.hhplus.be.server.infrastructure.coupon.repository.CouponIssueJpaRepository;
import kr.hhplus.be.server.infrastructure.coupon.repository.CouponJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
public class CouponConcurrencyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CouponJpaRepository couponJpaRepository;

    @Autowired
    private CouponIssueJpaRepository couponIssueJpaRepository;

    private String nowPlus(int days) {
        return java.time.LocalDateTime.now().plusDays(days).toString();
    }

    @Test
    @DisplayName("동시성 테스트: 20명의 유저가 동시에 쿠폰 발급 요청을 보냈을 때 실제 발급된 수가 초과될 수 있다")
    void concurrent_coupon_issue_test() throws InterruptedException {
        // 발급 가능한 수량이 3개인 쿠폰 등록
        Coupon coupon = Coupon.builder()
                .type(CouponType.FIXED)
                .description("동시성 테스트 쿠폰")
                .discount(1000)
                .quantity(3)
                .issued(0)
                .expirationDays(7)
                .createdAt(nowPlus(-1))
                .updatedAt(nowPlus(1))
                .build();
        Coupon getCoupon = couponJpaRepository.save(coupon);
        Long couponId = getCoupon.getId();

        int threadCount = 20;
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (long userId = 1; userId <= threadCount; userId++) {
            final long uid = userId;
            new Thread(() -> {
                try {
                    mockMvc.perform(post("/v1/coupon/" + couponId + "/issue")
                                    .header("userId", uid)
                                    .contentType(MediaType.APPLICATION_JSON))
                            .andReturn();
                } catch (Exception e) {
                    System.out.println("에러 발생: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        long actualIssued = couponIssueJpaRepository.findAll().stream()
                .filter(issue -> issue.getCouponId().equals(couponId))
                .count();

        System.out.println("최종 발급된 쿠폰 수: " + actualIssued);

        assertThat(actualIssued).isEqualTo(3);
    }
}
