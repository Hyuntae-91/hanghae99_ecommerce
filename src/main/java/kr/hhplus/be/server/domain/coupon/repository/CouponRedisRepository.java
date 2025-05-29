package kr.hhplus.be.server.domain.coupon.repository;

import kr.hhplus.be.server.domain.coupon.dto.response.CouponIssueRedisDto;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CouponRedisRepository {
    boolean decreaseStock(Long couponId);
    boolean addCouponForUser(Long userId, Long couponId, Long couponIssueId, Integer use);
    boolean updateCouponIssueId(Long userId, Long couponId, Long couponIssueId);
    void saveCouponInfo(Long couponId, String couponInfoJson);
    boolean existsStock(Long couponId);
    void saveStock(Long couponId, int quantity);
    Optional<String> findCouponInfo(Long couponId);
    Map<Long, CouponIssueRedisDto> findAllIssuedCoupons(Long userId);
    List<String> findCouponInfos(List<Long> couponIds);
    void updateCouponUse(Long userId, Long couponId, int status);
}