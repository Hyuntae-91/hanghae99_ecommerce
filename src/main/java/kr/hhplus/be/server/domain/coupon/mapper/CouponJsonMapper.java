package kr.hhplus.be.server.domain.coupon.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.hhplus.be.server.domain.coupon.dto.response.CouponDto;
import kr.hhplus.be.server.domain.coupon.model.Coupon;

import java.util.List;
import java.util.Objects;

public class CouponJsonMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private CouponJsonMapper() {
        // static only
    }

    public static String toCouponJson(Coupon coupon) {
        try {
            return objectMapper.writeValueAsString(coupon);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Coupon을 JSON으로 변환할 수 없습니다.", e);
        }
    }

    public static Coupon fromCouponJson(String json) {
        try {
            return objectMapper.readValue(json, Coupon.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON을 Coupon으로 변환할 수 없습니다.", e);
        }
    }

    public static List<CouponDto> fromCouponJsonList(List<String> couponJsonList) {
        if (couponJsonList == null || couponJsonList.isEmpty()) {
            return List.of();
        }
        return couponJsonList.stream()
                .filter(Objects::nonNull) // 혹시 null 들어있는 경우 필터링
                .map(json -> {
                    Coupon coupon = fromCouponJson(json);
                    return CouponDto.from(coupon);
                })
                .toList();
    }
}
