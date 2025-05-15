package kr.hhplus.be.server.infrastructure.coupon;

import kr.hhplus.be.server.domain.coupon.repository.CouponRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CouponRedisRepositoryImpl implements CouponRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public boolean decreaseStock(Long couponId) {
        String stockKey = getStockKey(couponId);
        Long result = redisTemplate.opsForValue().decrement(stockKey);
        return result != null && result >= 0;
    }

    @Override
    public boolean addCouponForUser(Long userId, Long couponId, Integer use) {
        String key = getIssuedKey(userId);
        Boolean exists = redisTemplate.opsForHash().hasKey(key, couponId.toString());
        if (Boolean.TRUE.equals(exists)) {
            return false; // 이미 발급된 경우
        }

        redisTemplate.opsForHash().put(key, couponId.toString(), use.toString());
        return true;
    }

    @Override
    public boolean existsStock(Long couponId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(getStockKey(couponId)));
    }

    @Override
    public void saveStock(Long couponId, int quantity) {
        redisTemplate.opsForValue().set(getStockKey(couponId), String.valueOf(quantity));
    }

    @Override
    public void saveCouponInfo(Long couponId, String couponInfoJson) {
        String key = "coupon:info:" + couponId;
        redisTemplate.opsForValue().set(key, couponInfoJson);
    }


    @Override
    public Optional<String> findCouponInfo(Long couponId) {
        String key = "coupon:info:" + couponId;
        String value = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(value);
    }

    @Override
    public Map<Long, Integer> findAllIssuedCoupons(Long userId) {
        String key = getIssuedKey(userId);

        Map<Object, Object> rawEntries = redisTemplate.opsForHash().entries(key);
        return rawEntries.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> Long.parseLong(e.getKey().toString()),
                        e -> Integer.parseInt(e.getValue().toString())
                ));
    }


    @Override
    public void updateCouponUse(Long userId, Long couponId) {
        String key = getIssuedKey(userId);
        String field = String.valueOf(couponId);

        redisTemplate.opsForHash().put(key, field, "1");
    }

    @Override
    public void rollbackCoupon(Long userId, Long couponId) {
        String key = getIssuedKey(userId);
        String field = String.valueOf(couponId);

        // 발급된 쿠폰을 다시 미사용(0)으로 복구
        redisTemplate.opsForHash().put(key, field, "0");
    }

    @Override
    public List<String> findCouponInfos(List<Long> couponIds) {
        List<String> keys = couponIds.stream()
                .map(id -> "coupon:info:" + id)
                .toList();

        return redisTemplate.opsForValue().multiGet(keys);
    }

    private String getStockKey(Long couponId) {
        return "coupon:stock:" + couponId;
    }

    private String getIssuedKey(Long userId) {
        return "coupon:issued:" + userId;
    }
}