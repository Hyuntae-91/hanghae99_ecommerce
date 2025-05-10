package kr.hhplus.be.server.domain.point.service;

import kr.hhplus.be.server.common.aop.lock.DistributedLock;
import kr.hhplus.be.server.domain.point.dto.UserPointMapper;
import kr.hhplus.be.server.domain.point.dto.request.PointChargeServiceRequest;
import kr.hhplus.be.server.domain.point.dto.request.PointUseServiceRequest;
import kr.hhplus.be.server.domain.point.dto.request.PointValidateUsableRequest;
import kr.hhplus.be.server.domain.point.dto.request.UserPointServiceRequest;
import kr.hhplus.be.server.domain.point.dto.response.PointChargeServiceResponse;
import kr.hhplus.be.server.domain.point.dto.response.PointUseServiceResponse;
import kr.hhplus.be.server.domain.point.dto.response.UserPointServiceResponse;
import kr.hhplus.be.server.domain.point.model.*;
import kr.hhplus.be.server.domain.point.repository.PointHistoryRepository;
import kr.hhplus.be.server.domain.point.repository.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final UserPointMapper userPointMapper;

    @Transactional
    @Cacheable(value = "userPoint", key = "#root.args[0].userId()")
    public UserPointServiceResponse getUserPoint(UserPointServiceRequest reqService) {
        log.info("DB 조회: userId={}", reqService.userId());
        return userPointMapper.toUserPointResponse(pointRepository.findWithLockByUserId(reqService.userId()));
    }

    @DistributedLock(key = "'lock:point:user:' + #arg0.userId")
    @CacheEvict(value = "userPoint", key = "#root.args[0].userId()")
    public PointChargeServiceResponse charge(PointChargeServiceRequest reqService) {
        UserPoint userPoint = pointRepository.findWithLockByUserId(reqService.userId());
        userPoint.charge(reqService.point());
        pointRepository.savePoint(userPoint);
        pointHistoryRepository.saveHistory(reqService.userId(), reqService.point(), PointHistoryType.CHARGE);

        return userPointMapper.toUserPointChargeResponse(userPoint);
    }

    @Transactional
    @CacheEvict(value = "userPoint", key = "#root.args[0].userId()")
    public PointUseServiceResponse use(PointUseServiceRequest reqService) {
        UserPoint userPoint = pointRepository.findWithLockByUserId(reqService.userId());
        userPoint.use(reqService.point());
        pointRepository.savePoint(userPoint);
        pointHistoryRepository.saveHistory(reqService.userId(), reqService.point(), PointHistoryType.USE);

        return userPointMapper.toUserPointUseResponse(userPoint);
    }

    public void validateUsable(PointValidateUsableRequest reqService) {
        UserPoint userPoint = pointRepository.get(reqService.userId());
        userPoint.validateUsableBalance(reqService.totalPrice());
    }
}
