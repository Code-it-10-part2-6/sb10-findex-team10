package com.sb10findexteam6.service;

import com.sb10findexteam6.common.enums.Result;
import com.sb10findexteam6.common.exception.BusinessException;
import com.sb10findexteam6.common.exception.ErrorCode;
import com.sb10findexteam6.dto.syncJob.CursorPageResponseSyncJobDto;
import com.sb10findexteam6.dto.syncJob.IndexDataSyncRequest;
import com.sb10findexteam6.dto.syncJob.SyncJobDto;
import com.sb10findexteam6.dto.syncJob.SyncJobSearchCondition;
import com.sb10findexteam6.dto.syncJob.SyncJobSummaryDto;
import com.sb10findexteam6.entity.IndexData;
import com.sb10findexteam6.entity.IndexInfo;
import com.sb10findexteam6.entity.SyncJob;
import com.sb10findexteam6.mapper.SyncJobMapper;
import com.sb10findexteam6.repository.IndexDataRepository;
import com.sb10findexteam6.repository.IndexInfoRepository;
import com.sb10findexteam6.repository.SyncJobRepository;
import com.sb10findexteam6.repository.specification.SyncJobSpecification;
import com.sb10findexteam6.service.openapi.OpenApiFetchService;
import com.sb10findexteam6.service.openapi.OpenApiSyncService;
import com.sb10findexteam6.service.openapi.SyncDataPersistenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class SyncJobServiceImpl implements SyncJobService {

    private static final int DEFAULT_SIZE = 10;
    private static final String DEFAULT_SORT_FIELD = "jobTime";

    private final SyncJobRepository syncJobRepository;
    private final IndexInfoRepository indexInfoRepository;
    private final OpenApiSyncService openApiSyncService;
    private final SyncDataPersistenceService syncDataPersistenceService;
    private final IndexInfoService indexInfoService;

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponseSyncJobDto getAll(SyncJobSearchCondition condition) {
        int size = resolveSize(condition.size());
        Sort sort = createSort(condition.sortField(), condition.sortDirection());

        Specification<SyncJob> filterSpec = buildFilterSpec(condition);
        Specification<SyncJob> cursorSpec = buildCursorSpec(condition.idAfter(), sort);

        List<SyncJob> queried = syncJobRepository.findAll(
                Specification.<SyncJob>unrestricted()
                        .and(filterSpec)
                        .and(cursorSpec),
                PageRequest.of(0, size + 1, sort)
        ).getContent();

        boolean hasNext = queried.size() > size;
        List<SyncJob> pageContent = hasNext ? queried.subList(0, size) : queried;

        List<SyncJobDto> content = SyncJobMapper.toDtoList(pageContent);

        Long nextIdAfter = content.isEmpty()
                ? null
                : content.get(content.size() - 1).id();

        String nextCursor = nextIdAfter == null ? null : String.valueOf(nextIdAfter);

        long totalElements = syncJobRepository.count(filterSpec);

        return new CursorPageResponseSyncJobDto(
                content,
                nextCursor,
                nextIdAfter,
                size,
                totalElements,
                hasNext
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SyncJobSummaryDto getSummary() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        long successCount = syncJobRepository.count(
                Specification.<SyncJob>unrestricted()
                        .and(SyncJobSpecification.hasResult(Result.SUCCESS))
                        .and(SyncJobSpecification.jobTimeGoe(sevenDaysAgo))
        );

        long failureCount = syncJobRepository.count(
                Specification.<SyncJob>unrestricted()
                        .and(SyncJobSpecification.hasResult(Result.FAILED))
                        .and(SyncJobSpecification.jobTimeGoe(sevenDaysAgo))
        );

        List<SyncJob> latest = syncJobRepository.findAll(
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "jobTime"))
        ).getContent();

        LocalDateTime lastSyncTime = latest.isEmpty()
                ? null
                : latest.get(0).getJobTime();

        return new SyncJobSummaryDto(successCount, failureCount, lastSyncTime);
    }

    @Override
    public List<SyncJobDto> syncIndexInfos(String worker) {
        // 데이터가 있는 날짜 최대 7일까지 탐색
        String resolvedWorker = resolveWorker(worker);

        LocalDate date = LocalDate.now();
        int maxFallbackDays = 7;

        for (int i = 0; i < maxFallbackDays; i++) {
            String targetDate = date.minusDays(i).format(DateTimeFormatter.BASIC_ISO_DATE);
            List<SyncJobDto> result = indexInfoService.syncFromOpenApi(targetDate, resolvedWorker);

            if (result != null && !result.isEmpty()) {
                return result;
            }
        }

        return List.of();
    }

    @Override
    public List<SyncJobDto> syncIndexData(IndexDataSyncRequest request, String worker) {
        validateIndexDataSyncRequest(request);

        String resolvedWorker = resolveWorker(worker);
        // 지수정보 목록 가져옴. -> [1,2,3] 요청
        List<IndexInfo> targetIndexes = resolveTargetIndexes(request.indexInfoIds());
        // 결과 저장용 리스트
        List<SyncJobDto> result = new ArrayList<>();
        // 동기화 시작 날짜
        LocalDate targetDate = request.baseDateFrom();

        while (!targetDate.isAfter(request.baseDateTo())) {
            for (IndexInfo indexInfo : targetIndexes) {
                try {
                    // 실제 openApi 데이터 조회
                    Optional<IndexData> fetchedDataOpt =
                            openApiSyncService.fetchOneDayIndexData(indexInfo, targetDate);
                    // 데이터가 존재하면 -> 저장 + 성공 기록!
                    if (fetchedDataOpt.isPresent()) {
                        syncDataPersistenceService.saveOneDayDataAndSuccessJob(
                                indexInfo,
                                fetchedDataOpt.get(),
                                targetDate,
                                resolvedWorker
                        );
                    } else {
                        // 휴장일이나 응답 없음 등 여러가지 경우가 존재하므로 실패아닌 정상 종료로 처리
                        syncDataPersistenceService.saveOneDaySuccessWithoutData(
                                indexInfo,
                                targetDate,
                                resolvedWorker
                        );
                    }
                    // 방금 저장된 최신 SyncJob 중 한 건 조회!! (최신)
                    SyncJobDto latestDto = syncJobRepository.findAll(
                                    PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "jobTime"))
                            ).getContent().stream()
                            .findFirst()
                            .map(SyncJobMapper::toDto)
                            .orElse(null);

                    if (latestDto != null) {
                        result.add(latestDto);
                    }

                } catch (Exception e) {
                    syncDataPersistenceService.saveOneDayFailedJob(
                            indexInfo,
                            targetDate,
                            resolvedWorker,
                            e.getMessage()
                    );
                    // 실패 후에도 최신 SyncJob 기록 result에 추가
                    SyncJobDto latestDto = syncJobRepository.findAll(
                                    PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "jobTime"))
                            ).getContent().stream()
                            .findFirst()
                            .map(SyncJobMapper::toDto)
                            .orElse(null);

                    if (latestDto != null) {
                        result.add(latestDto);
                    }
                }
            }
            // 다음 날짜로 이동 시켜 while문 반복! 종료일까지
            targetDate = targetDate.plusDays(1);
        }

        return result;
    }

    private Specification<SyncJob> buildFilterSpec(SyncJobSearchCondition condition) {
        return Specification.<SyncJob>unrestricted()
                .and(SyncJobSpecification.hasJobType(condition.jobType()))
                .and(SyncJobSpecification.hasIndexInfoId(condition.indexInfoId()))
                .and(SyncJobSpecification.targetDateGoe(condition.targetDateFrom()))
                .and(SyncJobSpecification.targetDateLoe(condition.targetDateTo()))
                .and(SyncJobSpecification.workerContains(condition.worker()))
                .and(SyncJobSpecification.hasResult(condition.result()));
    }

    private Specification<SyncJob> buildCursorSpec(Long idAfter, Sort sort) {
        if (idAfter == null) {
            return null;
        }

        Sort.Order idOrder = sort.getOrderFor("id");
        Sort.Direction direction = idOrder != null ? idOrder.getDirection() : Sort.Direction.DESC;

        return (root, query, cb) -> {
            if (direction.isAscending()) {
                return cb.greaterThan(root.get("id"), idAfter);
            }
            return cb.lessThan(root.get("id"), idAfter);
        };
    }

    private Sort createSort(String sortField, String sortDirection) {
        String resolvedField = resolveSortField(sortField);
        Sort.Direction direction = resolveDirection(sortDirection);

        return Sort.by(direction, resolvedField)
                .and(Sort.by(direction, "id"));
    }

    private String resolveSortField(String sortField) {
        if ("targetDate".equals(sortField)) {
            return "targetDate";
        }
        return DEFAULT_SORT_FIELD;
    }

    private Sort.Direction resolveDirection(String sortDirection) {
        if ("asc".equalsIgnoreCase(sortDirection)) {
            return Sort.Direction.ASC;
        }
        return Sort.Direction.DESC;
    }

    private int resolveSize(Integer size) {
        if (size == null || size <= 0) {
            return DEFAULT_SIZE;
        }
        return size;
    }

    private String resolveWorker(String worker) {
        return (worker == null || worker.isBlank()) ? "system" : worker;
    }

    private void validateIndexDataSyncRequest(IndexDataSyncRequest request) {
        if (request == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "연동 요청 정보가 없습니다."
            );
        }

        if (request.baseDateFrom() == null || request.baseDateTo() == null) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "대상 날짜 범위는 반드시 지정해야 합니다."
            );
        }

        if (request.baseDateFrom().isAfter(request.baseDateTo())) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "시작 날짜는 종료 날짜보다 이후일 수 없습니다."
            );
        }
    }

    private List<IndexInfo> resolveTargetIndexes(List<Long> indexInfoIds) {
        if (indexInfoIds == null || indexInfoIds.isEmpty()) {
            return indexInfoRepository.findAll();
        }

        List<IndexInfo> indexes = indexInfoRepository.findAllById(indexInfoIds);

        if (indexes.size() != indexInfoIds.size()) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "존재하지 않는 지수 정보 ID가 포함되어 있습니다."
            );
        }

        return indexes;
    }
}