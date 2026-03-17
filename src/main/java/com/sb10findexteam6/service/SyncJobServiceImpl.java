package com.sb10findexteam6.service;

import com.sb10findexteam6.common.enums.JobType;
import com.sb10findexteam6.common.enums.Result;
import com.sb10findexteam6.common.enums.SourceType;
import com.sb10findexteam6.common.exception.BusinessException;
import com.sb10findexteam6.common.exception.ErrorCode;
import com.sb10findexteam6.dto.openapi.FscIndexResponseDto;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class SyncJobServiceImpl implements SyncJobService {

    private static final int DEFAULT_SIZE = 10;
    private static final String DEFAULT_SORT_FIELD = "jobTime";
    private static final int OPEN_API_NUM_OF_ROWS = 1000;

    private final SyncJobRepository syncJobRepository;
    private final IndexInfoRepository indexInfoRepository;
    private final IndexDataRepository indexDataRepository;
    private final OpenApiFetchService openApiFetchService;

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
        throw new UnsupportedOperationException("지수 정보 연동은 아직 미구현입니다.");
    }

    @Override
    public List<SyncJobDto> syncIndexData(IndexDataSyncRequest request, String worker) {
        validateSyncRequest(request);

        List<IndexInfo> targetIndexInfos = resolveTargetIndexInfos(request.indexInfoIds());
        List<SyncJob> savedJobs = new ArrayList<>();

        for (LocalDate targetDate = request.baseDateFrom();
             !targetDate.isAfter(request.baseDateTo());
             targetDate = targetDate.plusDays(1)) {

            Map<String, FscIndexResponseDto.Item> itemMap = fetchItemMapByDate(targetDate);

            for (IndexInfo indexInfo : targetIndexInfos) {
                LocalDateTime jobTime = LocalDateTime.now();

                try {
                    String key = makeItemKey(indexInfo.getIndexClassification(), indexInfo.getIndexName());
                    FscIndexResponseDto.Item item = itemMap.get(key);

                    if (item == null) {
                        savedJobs.add(saveSyncJob(indexInfo, JobType.INDEX_DATA, targetDate, worker, jobTime, Result.FAILED));
                        continue;
                    }

                    upsertIndexData(indexInfo, targetDate, item);
                    savedJobs.add(saveSyncJob(indexInfo, JobType.INDEX_DATA, targetDate, worker, jobTime, Result.SUCCESS));
                } catch (Exception e) {
                    savedJobs.add(saveSyncJob(indexInfo, JobType.INDEX_DATA, targetDate, worker, jobTime, Result.FAILED));
                }
            }
        }

        return SyncJobMapper.toDtoList(savedJobs);
    }

    private void validateSyncRequest(IndexDataSyncRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "요청 본문이 비어 있습니다.");
        }
        if (request.baseDateFrom() == null || request.baseDateTo() == null) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "시작 날짜와 종료 날짜는 필수입니다.");
        }
        if (request.baseDateFrom().isAfter(request.baseDateTo())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "시작 날짜는 종료 날짜보다 이후일 수 없습니다.");
        }
    }

    private List<IndexInfo> resolveTargetIndexInfos(List<Long> indexInfoIds) {
        if (indexInfoIds == null || indexInfoIds.isEmpty()) {
            return indexInfoRepository.findAll();
        }

        List<IndexInfo> indexInfos = indexInfoRepository.findAllById(indexInfoIds);
        if (indexInfos.size() != indexInfoIds.size()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "존재하지 않는 지수 정보 ID가 포함되어 있습니다.");
        }
        return indexInfos;
    }

    private Map<String, FscIndexResponseDto.Item> fetchItemMapByDate(LocalDate targetDate) {
        int pageNo = 1;
        int totalCount = Integer.MAX_VALUE;
        Map<String, FscIndexResponseDto.Item> itemMap = new LinkedHashMap<>();

        while ((pageNo - 1) * OPEN_API_NUM_OF_ROWS < totalCount) {
            FscIndexResponseDto response = openApiFetchService.fetchStockMarketIndex(
                    targetDate.format(DateTimeFormatter.BASIC_ISO_DATE),
                    OPEN_API_NUM_OF_ROWS,
                    pageNo
            );

            validateOpenApiResponse(response);

            FscIndexResponseDto.Body body = response.response().body();
            totalCount = body.totalCount();

            List<FscIndexResponseDto.Item> items =
                    body.items() == null || body.items().item() == null
                            ? List.of()
                            : body.items().item();

            for (FscIndexResponseDto.Item item : items) {
                itemMap.put(makeItemKey(item.idxCsf(), item.idxNm()), item);
            }

            if (items.isEmpty()) {
                break;
            }

            pageNo++;
        }

        return itemMap;
    }

    private void validateOpenApiResponse(FscIndexResponseDto response) {
        if (response == null || response.response() == null || response.response().header() == null) {
            throw new BusinessException(ErrorCode.OPEN_API_COMMUNICATION_ERROR, "공공데이터 응답 형식이 올바르지 않습니다.");
        }

        String resultCode = response.response().header().resultCode();
        if (!"00".equals(resultCode)) {
            throw new BusinessException(
                    ErrorCode.OPEN_API_COMMUNICATION_ERROR,
                    "공공데이터 응답 실패: " + resultCode + " / " + response.response().header().resultMsg()
            );
        }

        if (response.response().body() == null) {
            throw new BusinessException(ErrorCode.OPEN_API_COMMUNICATION_ERROR, "공공데이터 응답 body가 비어 있습니다.");
        }
    }

    private void upsertIndexData(IndexInfo indexInfo, LocalDate targetDate, FscIndexResponseDto.Item item) {
        IndexData existing = indexDataRepository
                .findByIndexInfoIdAndBaseDate(indexInfo.getId(), targetDate)
                .orElse(null);

        if (existing == null) {
            indexDataRepository.save(new IndexData(
                    indexInfo,
                    targetDate,
                    SourceType.OPEN_API,
                    parseBigDecimal(item.mkp()),
                    parseBigDecimal(item.clpr()),
                    parseBigDecimal(item.hipr()),
                    parseBigDecimal(item.lopr()),
                    parseBigDecimal(item.vs()),
                    parseBigDecimal(item.fltRt()),
                    parseLong(item.trqu()),
                    parseLong(item.trPrc()),
                    parseLong(item.lstgMrktTotAmt())
            ));
            return;
        }

        existing.update(
                parseBigDecimal(item.mkp()),
                parseBigDecimal(item.clpr()),
                parseBigDecimal(item.hipr()),
                parseBigDecimal(item.lopr()),
                parseBigDecimal(item.vs()),
                parseBigDecimal(item.fltRt()),
                parseLong(item.trqu()),
                parseLong(item.trPrc()),
                parseLong(item.lstgMrktTotAmt())
        );
    }

    private SyncJob saveSyncJob(
            IndexInfo indexInfo,
            JobType jobType,
            LocalDate targetDate,
            String worker,
            LocalDateTime jobTime,
            Result result
    ) {
        return syncJobRepository.save(new SyncJob(indexInfo, jobType, targetDate, worker, jobTime, result));
    }

    private String makeItemKey(String indexClassification, String indexName) {
        return indexClassification + "|" + indexName;
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.trim());
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return 0L;
        }
        return Long.parseLong(value.trim());
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
}