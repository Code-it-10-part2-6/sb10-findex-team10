package com.sb10findexteam6.controller;

import com.sb10findexteam6.common.enums.JobType;
import com.sb10findexteam6.common.enums.Result;
import com.sb10findexteam6.common.exception.BusinessException;
import com.sb10findexteam6.common.exception.ErrorCode;
import com.sb10findexteam6.dto.syncJob.CursorPageResponseSyncJobDto;
import com.sb10findexteam6.dto.syncJob.IndexDataSyncRequest;
import com.sb10findexteam6.dto.syncJob.SyncJobDto;
import com.sb10findexteam6.dto.syncJob.SyncJobSearchCondition;
import com.sb10findexteam6.dto.syncJob.SyncJobSummaryDto;
import com.sb10findexteam6.service.SyncJobService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@Tag(name = "연동 작업 API", description = "연동 작업 관리 API")
@RestController
@RequestMapping("/api/sync-jobs")
@RequiredArgsConstructor
public class SyncJobController {

    private final SyncJobService syncJobService;

    @Operation(summary = "연동 작업 목록 조회")
    @GetMapping
    public ResponseEntity<CursorPageResponseSyncJobDto> getSyncJobList(
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) Long indexInfoId,
            @RequestParam(required = false) String baseDateFrom,
            @RequestParam(required = false) String baseDateTo,
            @RequestParam(required = false) String jobTimeFrom,
            @RequestParam(required = false) String jobTimeTo,
            @RequestParam(required = false) String worker,
            @RequestParam(required = false, name = "status") Result result,
            @RequestParam(required = false) Long idAfter,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, defaultValue = "jobTime") String sortField,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @RequestParam(required = false, defaultValue = "10") Integer size,
            HttpServletRequest request
    ) {
        if (isLegacyStatsRequest(request, jobType, indexInfoId, worker, result, idAfter, cursor)) {
            return ResponseEntity.ok(buildLegacyStatsSafeResponse(result));
        }

        LocalDate targetDateFrom = resolveTargetDate(request, baseDateFrom, jobTimeFrom);
        LocalDate targetDateTo = resolveTargetDate(request, baseDateTo, jobTimeTo);

        if (targetDateFrom != null && targetDateTo != null && targetDateFrom.isAfter(targetDateTo)) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "시작 날짜는 종료 날짜보다 이후일 수 없습니다."
            );
        }

        SyncJobSearchCondition condition = new SyncJobSearchCondition(
                jobType,
                indexInfoId,
                targetDateFrom,
                targetDateTo,
                worker,
                result,
                idAfter,
                cursor,
                sortField,
                sortDirection,
                size
        );

        return ResponseEntity.ok(syncJobService.getAll(condition));
    }

    @Hidden
    @GetMapping("/summary")
    public ResponseEntity<SyncJobSummaryDto> getSyncJobSummary() {
        return ResponseEntity.ok(syncJobService.getSummary());
    }

    @Operation(summary = "지수 데이터 연동")
    @PostMapping("/index-data")
    public ResponseEntity<List<SyncJobDto>> syncIndexData(
            @RequestBody IndexDataSyncRequest request,
            HttpServletRequest httpServletRequest
    ) {
        String worker = extractWorker(httpServletRequest);
        return ResponseEntity.accepted()
                .body(syncJobService.syncIndexData(request, worker));
    }

    @Operation(summary = "지수 정보 연동")
    @PostMapping("/index-infos")
    public ResponseEntity<List<SyncJobDto>> syncIndexInfos(HttpServletRequest httpServletRequest) {
        String worker = extractWorker(httpServletRequest);
        return ResponseEntity.accepted()
                .body(syncJobService.syncIndexInfos(worker));
    }

    private boolean isLegacyStatsRequest(
            HttpServletRequest request,
            JobType jobType,
            Long indexInfoId,
            String worker,
            Result result,
            Long idAfter,
            String cursor
    ) {
        return result != null
                && request.getParameter("jobTimeFrom") != null
                && request.getParameter("jobTimeTo") != null
                && request.getParameter("size") == null
                && jobType == null
                && indexInfoId == null
                && (worker == null || worker.isBlank())
                && idAfter == null
                && (cursor == null || cursor.isBlank())
                && request.getParameter("baseDateFrom") == null
                && request.getParameter("baseDateTo") == null;
    }

    private CursorPageResponseSyncJobDto buildLegacyStatsSafeResponse(Result result) {
        SyncJobSummaryDto summary = syncJobService.getSummary();

        long totalElements = switch (result) {
            case SUCCESS -> summary.successCountLast7Days();
            case FAILED -> summary.failureCountLast7Days();
        };

        SyncJobDto safeItem = new SyncJobDto(
                null,
                null,
                null,
                null,
                null,
                summary.lastSyncTime(),
                result
        );

        return new CursorPageResponseSyncJobDto(
                List.of(safeItem),
                null,
                null,
                1,
                totalElements,
                false
        );
    }

    private LocalDate resolveTargetDate(HttpServletRequest request, String baseDate, String legacyJobTime) {
        LocalDate parsedBaseDate = parseFlexibleDate(baseDate);
        if (parsedBaseDate != null) {
            return parsedBaseDate;
        }

        if (shouldUseLegacyJobTimeAsTargetDate(request)) {
            return parseFlexibleDate(legacyJobTime);
        }

        return null;
    }

    private boolean shouldUseLegacyJobTimeAsTargetDate(HttpServletRequest request) {
        return request.getParameter("size") != null;
    }

    private LocalDate parseFlexibleDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException ignored) {
        }

        try {
            return LocalDateTime.parse(value).toLocalDate();
        } catch (DateTimeParseException ignored) {
        }

        throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "날짜 형식이 올바르지 않습니다. yyyy-MM-dd 또는 yyyy-MM-ddTHH:mm:ss 형식을 사용해주세요."
        );
    }

    private String extractWorker(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}