package com.sb10findexteam6.service;

import com.sb10findexteam6.common.enums.Result;
import com.sb10findexteam6.dto.syncJob.CursorPageResponseSyncJobDto;
import com.sb10findexteam6.dto.syncJob.IndexDataSyncRequest;
import com.sb10findexteam6.dto.syncJob.SyncJobDto;
import com.sb10findexteam6.dto.syncJob.SyncJobSearchCondition;
import com.sb10findexteam6.dto.syncJob.SyncJobSummaryDto;
import com.sb10findexteam6.entity.SyncJob;
import com.sb10findexteam6.mapper.SyncJobMapper;
import com.sb10findexteam6.repository.SyncJobRepository;
import com.sb10findexteam6.repository.specification.SyncJobSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

// syncIndexInfos, syncIndexData 는 외부 API 호출 로직 구현 후 다시 작성

@Service
@RequiredArgsConstructor
@Transactional
public class SyncJobServiceImpl implements SyncJobService {

    private static final int DEFAULT_SIZE = 10;
    private static final String DEFAULT_SORT_FIELD = "jobTime";
    private static final String DEFAULT_SORT_DIRECTION = "desc";

    private final SyncJobRepository syncJobRepository;

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
        throw new UnsupportedOperationException("지수 정보 연동은 아직 미구현.");
    }

    @Override
    public List<SyncJobDto> syncIndexData(IndexDataSyncRequest request, String worker) {
        throw new UnsupportedOperationException("지수 데이터 연동은 아직 미구현.");
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