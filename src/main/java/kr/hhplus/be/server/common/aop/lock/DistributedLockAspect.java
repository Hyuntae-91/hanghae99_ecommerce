package kr.hhplus.be.server.common.aop.lock;

import kr.hhplus.be.server.common.aop.AopForTransaction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DistributedLockAspect {

    private final RedissonClient redissonClient;
    private final AopForTransaction aopForTransaction;
    private final SpelExpressionParser parser = new SpelExpressionParser();

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String lockKey = parseKey(joinPoint, distributedLock.key());

        RLock lock = redissonClient.getLock(lockKey);
        boolean acquired = false;

        try {
            acquired = lock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), TimeUnit.SECONDS);

            if (!acquired) {
                log.warn("락 획득 실패: key={}", lockKey);
                throw new IllegalStateException("락 획득 실패");
            }

            log.info("락 획득 성공: key={}", lockKey);
            return aopForTransaction.proceed(joinPoint);
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("락 해제 완료: key={}", lockKey);
            }
        }
    }

    private String parseKey(ProceedingJoinPoint joinPoint, String keyExpression) {
        EvaluationContext context = new StandardEvaluationContext();
        Object[] args = joinPoint.getArgs();

        if (args.length > 0) {
            context.setVariable("arg0", args[0]);
        }

        return parser.parseExpression(keyExpression).getValue(context, String.class);
    }
}