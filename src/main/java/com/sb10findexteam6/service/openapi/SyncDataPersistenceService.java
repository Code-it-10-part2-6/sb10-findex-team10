package com.sb10findexteam6.service.openapi;

import com.sb10findexteam6.common.enums.JobType;
import com.sb10findexteam6.common.enums.Result;
import com.sb10findexteam6.entity.IndexData;
import com.sb10findexteam6.entity.IndexInfo;
import com.sb10findexteam6.entity.SyncJob;
import com.sb10findexteam6.repository.IndexDataRepository;
import com.sb10findexteam6.repository.SyncJobRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncDataPersistenceService {

    private final IndexDataRepository indexDataRepository;
    private final SyncJobRepository syncJobRepository;


    @Transactional
    public void saveOneDayDataAndSuccessJob(IndexInfo indexInfo, IndexData fetchedData, LocalDate targetDate, String worker) {

        Optional<IndexData> existingDataOpt = indexDataRepository.findByIndexInfoIdAndBaseDate(indexInfo.getId(), targetDate);

        if (existingDataOpt.isPresent()) {
            // 이미 데이터가 존재하면 Update 처리
            IndexData existingData = existingDataOpt.get();
            existingData.update(
                fetchedData.getMarketPrice(),
                fetchedData.getClosingPrice(),
                fetchedData.getHighPrice(),
                fetchedData.getLowPrice(),
                fetchedData.getVersus(),
                fetchedData.getFluctuationRate(),
                fetchedData.getTradingQuantity(),
                fetchedData.getTradingPrice(),
                fetchedData.getMarketTotalAmount()
            );
            log.info("[데이터 수정 완료(Update)] 지수={}, 날짜={}", indexInfo.getIndexName(), targetDate);
        } else {
            // 데이터가 없으면 신규 Insert 처리
            indexDataRepository.save(fetchedData);
            log.info("[데이터 저장 완료(Insert)] 지수={}, 날짜={}", indexInfo.getIndexName(), targetDate);
        }

        // 작업 성공 이력(SyncJob) 항상 기록
        SyncJob syncJob = new SyncJob(
            indexInfo,
            JobType.INDEX_DATA,
            targetDate,
            worker,
            LocalDateTime.now(),
            Result.SUCCESS
        );
        syncJobRepository.save(syncJob);
    }

    /**
     * 성공 처리: 수집된 데이터를 DB에 일괄 저장하고 SUCCESS 상태의 SyncJob을 기록
     * 하루 단위 성공 처리(데이터 없음): 휴장/미발표 등으로 IndexData가 없더라도 SUCCESS SyncJob을 남깁니다.
     */
    @Transactional
    public void saveOneDaySuccessWithoutData(IndexInfo indexInfo, LocalDate targetDate, String worker) {
        SyncJob syncJob = new SyncJob(
            indexInfo,
            JobType.INDEX_DATA,
            targetDate,
            worker,
            LocalDateTime.now(),
            Result.SUCCESS
        );
        syncJobRepository.save(syncJob);
        log.info("[동기화 성공(데이터 없음)] 지수={}, 날짜={}", indexInfo.getIndexName(), targetDate);
    }

    /**
     * 하루 단위 실패 처리: 상위 트랜잭션과 무관하게 FAILED SyncJob을 남깁니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveOneDayFailedJob(IndexInfo indexInfo, LocalDate targetDate, String worker, String failReason) {
        SyncJob syncJob = new SyncJob(
            indexInfo,
            JobType.INDEX_DATA,
            targetDate,
            worker,
            LocalDateTime.now(),
            Result.FAILED
        );
        syncJobRepository.save(syncJob);
        log.error("[Sync 실패 이력 기록 완료] 지수: {}, 타겟일자: {}, 사유: {}", indexInfo.getIndexName(), targetDate, failReason);
    }
}