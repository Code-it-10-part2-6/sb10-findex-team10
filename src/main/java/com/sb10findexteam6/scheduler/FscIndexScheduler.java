package com.sb10findexteam6.scheduler;

import com.sb10findexteam6.entity.IndexData;
import com.sb10findexteam6.entity.IndexInfo;
import com.sb10findexteam6.service.openapi.OpenApiSyncService;
import com.sb10findexteam6.service.openapi.SyncDataPersistenceService;
import com.sb10findexteam6.service.openapi.SyncTargetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FscIndexScheduler {

    private final SyncTargetService syncTargetService;
    private final OpenApiSyncService openApiSyncService;
    private final SyncDataPersistenceService syncDataPersistenceService;

    private static final String WORKER_NAME = "SYSTEM_SCHEDULER";

    /**
     * 매일 자정에 실행 (초 분 시 일 월 요일)
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void syncFscIndexData() {
        log.info("========== 공공데이터포털 지수 데이터 동기화 ==========");

        // 연동 대상 지수 목록 조회
        List<IndexInfo> targetIndexes = syncTargetService.getOpenApiTargetIndexes();

        for (IndexInfo indexInfo : targetIndexes) {
            // 각 지수별 연동 시작일 및 종료일 계산
            LocalDate startDate = syncTargetService.calculateNextSyncDate(indexInfo);
            LocalDate endDate = LocalDate.now().minusDays(1); // 주식 시장 마감을 고려해 무조건 '어제' 날짜까지 수집

            // 이미 최신화가 완료된 경우 (시작일이 어제보다 미래인 경우)
            if (startDate.isAfter(endDate)) {
                log.info("{} 지수는 이미 최신 상태입니다. (최근 연동일: {})", indexInfo.getIndexName(), endDate);
                continue; // 다음 지수로 넘어감
            }

            log.info("[스케줄러 진행] {} 지수 동기화 기간: {} ~ {}", indexInfo.getIndexName(), startDate, endDate);

            // 지수 1개가 부분 실패해도 다음 지수로 진행되도록, 지수 단위 루프를 유지
            // 지수 내부는 '하루 단위'로 성공/실패/중복을 기록합니다.
            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(endDate)) {
                try {
                    Optional<IndexData> indexDataOpt = openApiSyncService.fetchOneDayIndexData(indexInfo, currentDate);
                    if (indexDataOpt.isPresent()) {
                        syncDataPersistenceService.saveOneDayDataAndSuccessJob(
                            indexInfo,
                            indexDataOpt.get(),
                            currentDate,
                            WORKER_NAME
                        );
                    } else {
                        // 휴장/미발표 등: 데이터는 없지만, 해당 일자 동기화 시도는 정상 종료로 간주
                        syncDataPersistenceService.saveOneDaySuccessWithoutData(
                            indexInfo,
                            currentDate,
                            WORKER_NAME
                        );
                    }
                } catch (Exception e) {
                    log.error("[일자별 동기화 실패] 지수={}, 날짜={}, 메시지={}",
                        indexInfo.getIndexName(), currentDate, e.getMessage(), e);
                    syncDataPersistenceService.saveOneDayFailedJob(
                        indexInfo,
                        currentDate,
                        WORKER_NAME,
                        e.getMessage()
                    );
                } finally {
                    currentDate = currentDate.plusDays(1);
                }
            }
        }
        log.info("========== 공공데이터포털 지수 데이터 동기화 완료 ==========");
    }
}