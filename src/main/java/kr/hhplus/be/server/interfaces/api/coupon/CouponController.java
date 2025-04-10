package kr.hhplus.be.server.interfaces.api.coupon;

import kr.hhplus.be.server.exception.ErrorResponse;
import kr.hhplus.be.server.application.coupon.CouponIssueResponse;
import kr.hhplus.be.server.application.coupon.CouponListResponse;
import kr.hhplus.be.server.application.coupon.CouponResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CouponController implements CouponApi {

    @Override
    public ResponseEntity<?> getCoupons(@RequestHeader("userId") Long userId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(400, "Missing userId header"));
        }

        List<CouponResponse> coupons = List.of(
                new CouponResponse(1L, "PERCENT", "쿠폰 내용", 50L, 0,
                        "2025-04-03T09:00:00", "2025-04-04T09:00:00", "2025-04-03T09:00:00"),
                new CouponResponse(2L, "FIXED", "쿠폰 내용", 1000L, 0,
                        "2025-04-03T09:00:00", "2025-04-04T09:00:00", "2025-04-03T09:00:00")
        );

        return ResponseEntity.ok(new CouponListResponse(coupons));
    }

    @Override
    public ResponseEntity<?> issueCoupon(Long userId, Long couponId) {
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(400, "Missing userId header"));
        }

        if (couponId == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(404, "Coupon Not Found"));
        }

        if (couponId == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(409, "Coupon Out of Stock"));
        }

        CouponIssueResponse response = new CouponIssueResponse(
                couponId,
                "FIXED",
                "쿠폰 내용",
                1000L,
                0,
                "2025-04-03T09:00:00",
                "2025-04-04T09:00:00",
                "2025-04-03T09:00:00"
        );

        return ResponseEntity.ok(response);
    }
}

