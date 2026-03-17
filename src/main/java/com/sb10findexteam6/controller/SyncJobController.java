package com.sb10findexteam6.controller;

import com.sb10findexteam6.common.enums.JobType;
import com.sb10findexteam6.common.enums.Result;
import com.sb10findexteam6.dto.syncJob.CursorPageResponseSyncJobDto;
import com.sb10findexteam6.dto.syncJob.IndexDataSyncRequest;
import com.sb10findexteam6.dto.syncJob.SyncJobDto;
import com.sb10findexteam6.dto.syncJob.SyncJobSearchCondition;
import com.sb10findexteam6.dto.syncJob.SyncJobSummaryDto;
import com.sb10findexteam6.service.SyncJobService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sync-jobs")
public class SyncJobController {

    private final SyncJobService syncJobService;

    @GetMapping
    public ResponseEntity<CursorPageResponseSyncJobDto> getSyncJobList(
            @RequestParam(required = false) JobType jobType,
            @RequestParam(required = false) Long indexInfoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baseDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baseDateTo,
            @RequestParam(required = false) String worker,
            @RequestParam(required = false, name = "status") Result result,
            @RequestParam(required = false) Long idAfter,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false, defaultValue = "jobTime") String sortField,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection,
            @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        SyncJobSearchCondition condition = new SyncJobSearchCondition(
                jobType,
                indexInfoId,
                baseDateFrom,
                baseDateTo,
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

    @GetMapping("/summary")
    public ResponseEntity<SyncJobSummaryDto> getSyncJobSummary() {
        return ResponseEntity.ok(syncJobService.getSummary());
    }

    @PostMapping("/index-data")
    public ResponseEntity<List<SyncJobDto>> syncIndexData(
            @RequestBody IndexDataSyncRequest request,
            HttpServletRequest httpServletRequest
    ) {
        String worker = extractWorker(httpServletRequest);
        return ResponseEntity.accepted()
                .body(syncJobService.syncIndexData(request, worker));
    }

    @PostMapping("/index-infos")
    public ResponseEntity<List<SyncJobDto>> syncIndexInfos(HttpServletRequest httpServletRequest) {
        String worker = extractWorker(httpServletRequest);
        return ResponseEntity.accepted()
                .body(syncJobService.syncIndexInfos(worker));
    }

    private String extractWorker(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}