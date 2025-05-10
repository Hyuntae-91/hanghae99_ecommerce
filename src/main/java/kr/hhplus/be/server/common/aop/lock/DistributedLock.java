package kr.hhplus.be.server.common.aop.lock;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DistributedLock {
    String key();           // 락을 걸 Redis Key
    long waitTime() default 3L;    // 락 대기 시간 (초)
    long leaseTime() default 10L;  // 락 점유 시간 (초)
}