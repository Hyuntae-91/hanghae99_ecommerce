package kr.hhplus.be.server.infrastructure.product;

import kr.hhplus.be.server.domain.product.repository.ProductRankingRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductRankingRedisRepositoryImpl implements ProductRankingRedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean copyKey(String currentKey, String snapshotKey) {
        if (Boolean.FALSE.equals(redisTemplate.hasKey(currentKey))) {
            log.info("[ProductRankingRedisRepositoryImpl] No current key found: {}", currentKey);
            return false;
        }
        redisTemplate.opsForZSet().unionAndStore(currentKey, Set.of(), snapshotKey);
        return true;
    }

    @Override
    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public Set<ZSetOperations.TypedTuple<Object>> findAllWithScores(String key) {
        return redisTemplate.opsForZSet().rangeWithScores(key, 0, -1);
    }

    @Override
    public Set<ZSetOperations.TypedTuple<Object>> findTopNWithScores(String key, int n) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, n - 1);
    }

    @Override
    public void remove(String key, String member) {
        redisTemplate.opsForZSet().remove(key, member);
    }

    @Override
    public void updateScore(String key, String member, double newScore) {
        redisTemplate.opsForZSet().add(key, member, newScore);
    }

    @Override
    public void replaceCurrentWithTemp(String currentKey, String tempKey) {
        redisTemplate.delete(currentKey);
        redisTemplate.opsForZSet().unionAndStore(tempKey, Set.of(), currentKey);
    }

    @Override
    public void expire(String key, long timeoutSeconds) {
        redisTemplate.expire(key, java.time.Duration.ofSeconds(timeoutSeconds));
    }

    @Override
    public void expireAt(String key, Instant expireAt) {
        redisTemplate.expireAt(key, expireAt);
    }
}
