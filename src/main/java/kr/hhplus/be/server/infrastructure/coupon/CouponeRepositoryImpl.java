package kr.hhplus.be.server.infrastructure.coupon;

import kr.hhplus.be.server.domain.coupon.CouponRepository;
import kr.hhplus.be.server.domain.coupon.model.Coupon;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CouponeRepositoryImpl implements CouponRepository {

    @Override
    public Coupon findById(Long couponId) { return null; }

    @Override
    public Coupon save(Coupon coupon) { return coupon; }
}
