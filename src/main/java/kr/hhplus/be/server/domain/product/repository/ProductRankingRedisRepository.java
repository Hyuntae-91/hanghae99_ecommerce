package kr.hhplus.be.server.domain.product.repository;

import org.springframework.data.redis.core.ZSetOperations;

import java.time.Instant;
import java.util.Set;

public interface ProductRankingRedisRepository {

    boolean copyKey(String currentKey, String snapshotKey);

    void deleteKey(String key);

    Set<ZSetOperations.TypedTuple<Object>> findAllWithScores(String key);

    Set<ZSetOperations.TypedTuple<Object>> findTopNWithScores(String key, int n);

    void remove(String key, String member);

    void updateScore(String key, String member, double newScore);

    void replaceCurrentWithTemp(String currentKey, String tempKey);

    void expire(String key, long timeoutSeconds);

    void expireAt(String key, Instant expireAt);
}
