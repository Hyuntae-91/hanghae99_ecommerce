package kr.hhplus.be.server.infrastructure.coupon;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.domain.coupon.dto.response.CouponIssueRedisDto;
import kr.hhplus.be.server.domain.coupon.repository.CouponRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;

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
    public boolean addCouponForUser(Long userId, Long couponId, Long couponIssueId, Integer use) {
        String key = getIssuedKey(userId);
        Boolean exists = redisTemplate.opsForHash().hasKey(key, couponId.toString());
        if (Boolean.TRUE.equals(exists)) {
            return false; // 이미 발급된 경우
        }

        String value = String.format(
                "{\"couponIssueId\":%s,\"use\":%d}",
                couponIssueId != null ? couponIssueId.toString() : "null",
                use
        );
        redisTemplate.opsForHash().put(key, couponId.toString(), value);
        return true;
    }

    @Override
    public boolean updateCouponIssueId(Long userId, Long couponId, Long newCouponIssueId) {
        String key = getIssuedKey(userId);
        Object raw = redisTemplate.opsForHash().get(key, couponId.toString());
        if (raw == null) {
            return false;
        }

        try {
            String json = raw.toString();
            Map<String, Object> map = new HashMap<>();
            json = json.replaceAll("[{}\"]", "");
            for (String entry : json.split(",")) {
                String[] kv = entry.split(":");
                map.put(kv[0], kv[1].equals("null") ? null : kv[1]);
            }

            Integer use = map.get("use") != null ? Integer.parseInt((String) map.get("use")) : 0;

            String updated = String.format(
                    "{\"couponIssueId\":%s,\"use\":%d}",
                    newCouponIssueId != null ? newCouponIssueId.toString() : "null",
                    use
            );
            redisTemplate.opsForHash().put(key, couponId.toString(), updated);
            return true;
        } catch (Exception e) {
            throw new RuntimeException("couponIssueId 갱신 실패", e);
        }
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
    public Map<Long, CouponIssueRedisDto> findAllIssuedCoupons(Long userId) {
        String pattern = "coupon:issued:" + userId;
        Set<String> keys = redisTemplate.keys(pattern);

        if (keys.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, CouponIssueRedisDto> result = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        for (String key : keys) {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            for (Map.Entry<Object, Object> entry : entries.entrySet()) {
                Long couponId = Long.parseLong(entry.getKey().toString());
                Object value = entry.getValue();

                try {
                    CouponIssueRedisDto dto = objectMapper.readValue(value.toString(), CouponIssueRedisDto.class);
                    result.put(couponId, dto);
                } catch (JsonProcessingException e) {
                    throw new IllegalStateException("JSON 파싱 실패: " + value, e);
                }
            }
        }

        return result;
    }

    @Override
    public void updateCouponUse(Long userId, Long couponId, int status) {
        String key = getIssuedKey(userId);
        String field = String.valueOf(couponId);

        Object raw = redisTemplate.opsForHash().get(key, field);
        if (raw == null) {
            throw new IllegalStateException("해당 쿠폰이 존재하지 않습니다.");
        }
        try {
            String json = raw.toString();
            json = json.replaceAll("[{}\"]", "");
            Map<String, String> map = new HashMap<>();
            for (String entry : json.split(",")) {
                String[] kv = entry.split(":");
                map.put(kv[0], kv[1]);
            }

            String couponIssueIdStr = map.get("couponIssueId");
            Long couponIssueId = "null".equals(couponIssueIdStr) ? null : Long.parseLong(couponIssueIdStr);

            // 갱신된 JSON 생성
            String updated = String.format(
                    "{\"couponIssueId\":%s,\"use\":%d}",
                    couponIssueId != null ? couponIssueId.toString() : "null",
                    status
            );
            redisTemplate.opsForHash().put(key, field, updated);

        } catch (Exception e) {
            throw new RuntimeException("쿠폰 사용 상태 갱신 실패", e);
        }
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