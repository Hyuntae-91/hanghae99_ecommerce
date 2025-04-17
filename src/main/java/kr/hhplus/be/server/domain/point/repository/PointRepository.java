package kr.hhplus.be.server.domain.point.repository;

import kr.hhplus.be.server.domain.point.model.UserPoint;


public interface PointRepository {
    UserPoint get(Long userId);
    void savePoint(UserPoint userPoint);
}
