package com.sb10findexteam6.controller;
// 지수 데이터 자동연동 임시로 테스트하는 컨트롤러 입니다
import com.sb10findexteam6.dto.syncJob.IndexDataSyncRequest;
import com.sb10findexteam6.dto.syncJob.SyncJobDto;
import com.sb10findexteam6.service.SyncJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sync-jobs")
@RequiredArgsConstructor
public class SyncJobController {

    private final SyncJobService syncJobService;

    @PostMapping("/index-data")
    public List<SyncJobDto> syncIndexData(@RequestBody IndexDataSyncRequest request) {
        return syncJobService.syncIndexData(request, "manual");
    }
}