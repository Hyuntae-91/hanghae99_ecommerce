package kr.hhplus.be.server.interfaces.api.coupon;

import kr.hhplus.be.server.domain.coupon.service.CouponService;
import kr.hhplus.be.server.domain.coupon.dto.request.GetCouponsServiceRequest;
import kr.hhplus.be.server.domain.coupon.dto.response.GetCouponsServiceResponse;
import kr.hhplus.be.server.domain.coupon.dto.request.IssueNewCouponServiceRequest;
import kr.hhplus.be.server.domain.coupon.dto.response.IssueNewCouponServiceResponse;
import kr.hhplus.be.server.exception.ErrorResponse;
import kr.hhplus.be.server.interfaces.api.coupon.dto.CouponIssueResponse;
import kr.hhplus.be.server.interfaces.api.coupon.dto.CouponListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CouponController implements CouponApi {

    private final CouponService couponService;

    // TODO: 진행중인 쿠폰 배분 이벤트 추가 해볼만 하다

    @Override
    public ResponseEntity<?> getCoupons(@RequestHeader("userId") Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(400, "Missing userId header"));
        }
        GetCouponsServiceRequest getCouponServiceRequest = new GetCouponsServiceRequest(userId);
        GetCouponsServiceResponse result = couponService.getCoupons(getCouponServiceRequest);
        return ResponseEntity.ok(CouponListResponse.from(result));
    }

    @Override
    public ResponseEntity<?> issueCoupon(Long userId, Long couponId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(400, "Missing userId header"));
        }

        IssueNewCouponServiceResponse result = couponService.issueNewCoupon(
                new IssueNewCouponServiceRequest(userId, couponId)
        );
        return ResponseEntity.ok(CouponIssueResponse.from(result));
    }
}

