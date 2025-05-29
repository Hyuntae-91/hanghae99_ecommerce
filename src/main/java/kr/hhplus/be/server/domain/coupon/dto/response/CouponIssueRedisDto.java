package kr.hhplus.be.server.domain.coupon.dto.response;

import lombok.Getter;

@Getter
public class CouponIssueRedisDto {
    private Long couponIssueId;
    private int use;

    public CouponIssueRedisDto() {
    }

    public CouponIssueRedisDto(Long couponIssueId, int use) {
        this.couponIssueId = couponIssueId;
        this.use = use;
    }

    public void setCouponIssueId(Long couponIssueId) {
        this.couponIssueId = couponIssueId;
    }

    public void setUse(int use) {
        this.use = use;
    }

    @Override
    public String toString() {
        return "CouponIssueRedisDto{" +
                "couponIssueId=" + couponIssueId +
                ", use=" + use +
                '}';
    }
}
