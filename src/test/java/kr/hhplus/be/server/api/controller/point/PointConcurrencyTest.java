package kr.hhplus.be.server.api.controller.point;

import kr.hhplus.be.server.domain.point.dto.request.PointChargeServiceRequest;
import kr.hhplus.be.server.domain.point.dto.request.UserPointServiceRequest;
import kr.hhplus.be.server.domain.point.dto.response.UserPointServiceResponse;
import kr.hhplus.be.server.domain.point.model.UserPoint;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import kr.hhplus.be.server.domain.point.service.PointService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class PointConcurrencyTest {

    @Autowired
    private PointService pointService;

    @Autowired
    private PointRepository pointRepository;

    @Test
    @DisplayName("동시성 테스트: 여러 스레드가 동일한 유저에게 포인트를 동시에 충전")
    void concurrentChargeUserPoint_WithCountDownLatch() throws InterruptedException {
        long userId = 1L;
        int threadCount = 10;
        long chargeAmount = 1000;

        pointRepository.savePoint(new UserPoint(userId, 0L));
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            new Thread(() -> {
                try {
                    pointService.charge(new PointChargeServiceRequest(userId, chargeAmount));
                } catch (Exception e) {
                    System.err.println("충전 중 예외 발생: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            }).start();
        }
        latch.await();

        // 결과 검증
        UserPointServiceResponse result = pointService.getUserPoint(new UserPointServiceRequest(userId));
        assertEquals(threadCount * chargeAmount, result.point());
    }

    @Test
    @DisplayName("동시성 테스트: 여러 유저가 각각 동시에 포인트 동시에 충전")
    void concurrentChargeUserPointMultipleUsers() throws InterruptedException {
        int USER_COUNT = 3;
        int THREAD_COUNT_PER_USER = 10;
        long chargeAmount = 1000L;

        CountDownLatch latch = new CountDownLatch(USER_COUNT * THREAD_COUNT_PER_USER);
        for (long userId = 1L; userId <= USER_COUNT; userId++) {
            pointRepository.savePoint(new UserPoint(userId, 0L));
            for (int i = 0; i < THREAD_COUNT_PER_USER; i++) {
                final long uid = userId;
                new Thread(() -> {
                    try {
                        pointService.charge(new PointChargeServiceRequest(uid, chargeAmount));
                    } catch (Exception e) {
                        System.err.println("유저 " + uid + " 충전 중 예외 발생: " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                }).start();
            }
        }
        latch.await();

        for (long userId = 1L; userId <= USER_COUNT; userId++) {
            UserPointServiceResponse result = pointService.getUserPoint(new UserPointServiceRequest(userId));
            assertEquals(THREAD_COUNT_PER_USER * chargeAmount, result.point(), "유저 " + userId + " 포인트 불일치");
        }
    }
}
