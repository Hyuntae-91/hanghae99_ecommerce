package kr.hhplus.be.server.domain.product.service;

import kr.hhplus.be.server.domain.product.model.ProductRanking;
import kr.hhplus.be.server.domain.product.model.ProductScore;
import kr.hhplus.be.server.domain.product.model.mapper.ProductScoreMapper;
import kr.hhplus.be.server.domain.product.repository.ProductRankingRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


/*
- ZSET (Sorted Set)
- product:current, product:2025-05-14, product:temp:2025-05-14
- (member: 상품 ID, score: 실시간 점수)
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductRankingService {

    private final ProductRankingRedisRepository rankingRedisRepository;

    private static final String PRODUCT_CURRENT_KEY = "product:score:current";
    private static final String PRODUCT_WEEK_KEY = "product:score:week";
    private static final String PRODUCT_SNAPSHOT_KEY_PREFIX = "product:score:";
    private static final String PRODUCT_TEMP_KEY_PREFIX = "product:score:temp:";

    private static final LocalDate today = LocalDate.now();

    private static final int RANKING_TOP_N = 100;

    @Scheduled(cron = "0 50 23 * * *")
    public void updateDailyProductRanking() {
        log.info("[ProductRankingService] Start ranking update at 23:50");

        String todaySnapshotKey = PRODUCT_SNAPSHOT_KEY_PREFIX + today.format(DateTimeFormatter.ISO_DATE);
        String todayTempKey = PRODUCT_TEMP_KEY_PREFIX + today.format(DateTimeFormatter.ISO_DATE);

        // Step 1: product:score:current -> 오늘 날짜로 복제
        boolean currentExists = rankingRedisRepository.copyKey(PRODUCT_CURRENT_KEY, todaySnapshotKey);
        if (!currentExists) {
            log.info("[ProductRankingService] No current ranking key exists. Skipping ranking update.");
            return;
        }
        rankingRedisRepository.expire(todaySnapshotKey,  8 * 24 * 60 * 60);  // 복제 후 8일 TTL 설정
        log.info("[ProductRankingService] Set TTL 8 days for snapshot key: {}", todaySnapshotKey);
        log.info("[ProductRankingService] Copied product:current to snapshot key: {}", todaySnapshotKey);

        // Step 2: snapshot 복제 → temp 키
        rankingRedisRepository.copyKey(todaySnapshotKey, todayTempKey);
        log.info("[ProductRankingService] Copied snapshot to temp key: {}", todayTempKey);

        // Step 3: temp 키 데이터 조회 → ProductRanking 생성
        Set<ZSetOperations.TypedTuple<Object>> tempEntries = rankingRedisRepository.findAllWithScores(todayTempKey);
        List<ProductScore> scores = ProductScoreMapper.fromRedisTypedTuples(tempEntries);
        ProductRanking ranking = new ProductRanking(scores);

        // Step 4: 상위 N개만 남기고 나머지 삭제
        List<ProductScore> topScores = ranking.topN(RANKING_TOP_N);
        Set<Long> topProductIds = ProductScoreMapper.toProductIdSet(topScores);

        for (ProductScore score : scores) {
            if (!topProductIds.contains(score.productId())) {
                rankingRedisRepository.remove(todayTempKey, score.productId().toString());
                log.info("[ProductRankingService] Removed productId={} because it is not in top {}", score.productId(), RANKING_TOP_N);
                continue;
            }

            // Step 5: 임계값 이하면 삭제
            if (score.isBelowThreshold()) {
                rankingRedisRepository.remove(todayTempKey, score.productId().toString());
                log.info("[ProductRankingService] Removed productId={} due to low score={}", score.productId(), score.score());
                continue;
            }

            // Step 6: 감쇠 적용
            ProductScore decayed = score.decay();
            rankingRedisRepository.updateScore(todayTempKey, decayed.productId().toString(), decayed.score());
            log.info("[ProductRankingService] Decayed productId={} to new score={}", decayed.productId(), decayed.score());
        }

        // Step 7: temp 키 → product:score:current 이동
        rankingRedisRepository.replaceCurrentWithTemp(PRODUCT_CURRENT_KEY, todayTempKey);
        log.info("[ProductRankingService] Updated product:current from temp key: {}", todayTempKey);

        log.info("[ProductRankingService] Finished ranking update for date {}", today);
    }

    public void generateWeeklyRanking() {
        log.info("[ProductRankingService] Start generating weekly ranking");

        LocalDate today = LocalDate.now();

        // 1주일치 스냅샷 수집
        List<Set<ZSetOperations.TypedTuple<Object>>> snapshots = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            String snapshotKey = PRODUCT_SNAPSHOT_KEY_PREFIX + day.format(DateTimeFormatter.ISO_DATE);
            Set<ZSetOperations.TypedTuple<Object>> dailySnapshot = rankingRedisRepository.findAllWithScores(snapshotKey);
            snapshots.add(dailySnapshot);
        }

        // 스냅샷 합산 → ProductRanking 생성
        ProductRanking aggregatedRanking = ProductRanking.fromSnapshots(snapshots);

        // 상위 100개만 추출
        List<ProductScore> topScores = aggregatedRanking.topN(RANKING_TOP_N);

        // Redis에 저장
        for (ProductScore score : topScores) {
            rankingRedisRepository.updateScore(PRODUCT_WEEK_KEY, score.productId().toString(), score.score());
        }

        // TTL 1일 + 1시간 설정
        rankingRedisRepository.expire(PRODUCT_WEEK_KEY, (24 * 60 * 60) + 3600);

        log.info("[ProductRankingService] Weekly ranking generated and stored to Redis with key: {}", PRODUCT_WEEK_KEY);
    }
}
