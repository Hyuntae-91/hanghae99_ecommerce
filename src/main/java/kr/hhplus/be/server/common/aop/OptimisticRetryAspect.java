package kr.hhplus.be.server.common.aop;

import kr.hhplus.be.server.common.annotation.OptimisticRetry;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class OptimisticRetryAspect {

    @Around("@annotation(optimisticRetry)")
    public Object retry(ProceedingJoinPoint joinPoint, OptimisticRetry optimisticRetry) throws Throwable {
        int maxAttempts = optimisticRetry.maxAttempts();
        int attempt = 0;

        while (true) {
            try {
                log.info("🔥 OptimisticRetryAspect triggered for: {}", joinPoint.getSignature());
                return joinPoint.proceed();
            } catch (ObjectOptimisticLockingFailureException e) {
                attempt++;
                log.warn("Optimistic lock failure attempt {}/{}", attempt, maxAttempts);
                if (attempt >= maxAttempts) {
                    log.error("Max retry attempts reached");
                    throw e;
                }
                Thread.sleep((long) Math.pow(2, attempt) * 50L);
            }
        }
    }
}