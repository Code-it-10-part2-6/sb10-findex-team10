package com.sb10findexteam6.service.openapi;

import com.sb10findexteam6.common.config.properties.OpenApiProperties;
import com.sb10findexteam6.common.enums.JobType;
import com.sb10findexteam6.common.enums.Result;
import com.sb10findexteam6.common.enums.SourceType;
import com.sb10findexteam6.entity.IndexInfo;
import com.sb10findexteam6.repository.IndexInfoRepository;
import com.sb10findexteam6.repository.SyncJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SyncTargetService {

    private final IndexInfoRepository indexInfoRepository;
    private final SyncJobRepository syncJobRepository;
    private final OpenApiProperties openApiProperties;


    // 연동해야 할 지수 목록을 반환
    public List<IndexInfo> getOpenApiTargetIndexes() {
        return indexInfoRepository.findAllBySourceType(SourceType.OPEN_API);
    }

    /**
     * 특정 지수에 대해 오늘 연동을 시작해야 할 기준일(Next Target Date)을 계산합니다.
     */
    public LocalDate calculateNextSyncDate(IndexInfo indexInfo) {
        Optional<LocalDate> lastSuccessDate = syncJobRepository.findLatestTargetDate(
            indexInfo, JobType.INDEX_DATA, Result.SUCCESS);

        if (lastSuccessDate.isPresent()) {
            // 이전에 성공한 기록이 있다면 그 다음 날짜부터 연동 시작
            LocalDate nextDate = lastSuccessDate.get().plusDays(1);
            log.info("[SyncTarget] {} 지수: 마지막 성공일 {}, 연동 시작일 {}", indexInfo.getIndexName(), lastSuccessDate.get(), nextDate);
            return nextDate;
        } else {
            // yaml 파일에 설정된 일수만큼 과거로 돌아감
            return LocalDate.now().minusDays(openApiProperties.getDefaultSyncDays());
        }
    }
}