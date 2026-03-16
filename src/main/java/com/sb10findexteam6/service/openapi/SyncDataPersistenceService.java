package com.sb10findexteam6.service.openapi;

import com.sb10findexteam6.common.enums.JobType;
import com.sb10findexteam6.common.enums.Result;
import com.sb10findexteam6.entity.IndexData;
import com.sb10findexteam6.entity.IndexInfo;
import com.sb10findexteam6.entity.SyncJob;
import com.sb10findexteam6.repository.IndexDataRepository;
import com.sb10findexteam6.repository.SyncJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
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

    /**
     * 하루 단위 성공 처리: (가능하면) IndexData 저장 후, SUCCESS SyncJob을 남깁니다.
     * - 이미 저장된 데이터(중복)는 스킵하고도 SUCCESS로 기록합니다.
     * - race condition 등으로 인한 UNIQUE 제약 위반이 나더라도 해당 일자는 스킵 처리합니다.
     */
    @Transactional
    public void saveOneDayDataAndSuccessJob(IndexInfo indexInfo, IndexData indexData, LocalDate targetDate, String worker) {
        try {
            boolean exists = indexDataRepository.existsByIndexInfoIdAndBaseDate(indexInfo.getId(), targetDate);
            if (exists) {
                log.info("[중복 스킵] 지수={}, 날짜={} - 이미 저장된 데이터 존재", indexInfo.getIndexName(), targetDate);
            } else {
                indexDataRepository.save(indexData);
                log.info("[DB 저장 완료] 지수={}, 날짜={}", indexInfo.getIndexName(), targetDate);
            }
        } catch (DataIntegrityViolationException e) {
            log.warn("[중복 충돌] 지수={}, 날짜={} - 유니크 제약 위반, 해당 일자 데이터는 스킵 처리", indexInfo.getIndexName(), targetDate);
        }

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

    /**
     * 성공 처리: 수집된 데이터를 DB에 일괄 저장하고 SUCCESS 상태의 SyncJob을 기록
     */
    @Transactional
    public void saveSuccessJob(IndexInfo indexInfo, List<IndexData> indexDataList, LocalDate targetDate, String worker) {
        // 데이터가 존재하면 일괄 저장 (휴장일이라 데이터가 0개일 수도 있음)
        if (!indexDataList.isEmpty()) {
            indexDataRepository.saveAll(indexDataList);
            log.info("[DB 저장 완료] {} 지수의 IndexData {}건 정상 저장", indexInfo.getIndexName(), indexDataList.size());
        } else {
            log.info("[DB 저장 스킵] {} 지수의 저장할 IndexData 없음 (휴장일 추정)", indexInfo.getIndexName());
        }

        // 2. 성공 이력(SyncJob) 생성 및 저장
        SyncJob syncJob = new SyncJob(
            indexInfo,
            JobType.INDEX_DATA,
            targetDate,   // 어디까지 연동했는지 날짜 기록
            worker,       // "SYSTEM" 또는 "SCHEDULER"
            LocalDateTime.now(),
            Result.SUCCESS
        );
        syncJobRepository.save(syncJob);
    }

    /**
     * [실패 처리] 에러 발생 시 롤백에 휩쓸리지 않고 FAILED 상태의 SyncJob을 독립적으로 기록
     * REQUIRES_NEW: 기존 트랜잭션이 터져서 롤백되더라도, 이 메서드는 새로운 트랜잭션을 열어 무조건 DB에 커밋
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailedJob(IndexInfo indexInfo, LocalDate targetDate, String worker) {
        SyncJob syncJob = new SyncJob(
            indexInfo,
            JobType.INDEX_DATA,
            targetDate,
            worker,
            LocalDateTime.now(),
            Result.FAILED
        );
        syncJobRepository.save(syncJob);
        log.error("[Sync 실패 이력 기록 완료] 지수: {}, 타겟일자: {}", indexInfo.getIndexName(), targetDate);
    }
}