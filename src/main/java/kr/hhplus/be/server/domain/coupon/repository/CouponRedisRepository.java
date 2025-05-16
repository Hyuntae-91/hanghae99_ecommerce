package kr.hhplus.be.server.domain.coupon.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CouponRedisRepository {
    boolean decreaseStock(Long couponId);
    boolean addCouponForUser(Long userId, Long couponId, Integer use);
    void saveCouponInfo(Long couponId, String couponInfoJson);
    boolean existsStock(Long couponId);
    void saveStock(Long couponId, int quantity);
    Optional<String> findCouponInfo(Long couponId);
    Map<Long, Integer> findAllIssuedCoupons(Long userId);
    List<String> findCouponInfos(List<Long> couponIds);
    void updateCouponUse(Long userId, Long couponId);
    void rollbackCoupon(Long userId, Long couponId);
}