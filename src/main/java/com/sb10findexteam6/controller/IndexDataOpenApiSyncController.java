package com.sb10findexteam6.controller;

import com.sb10findexteam6.dto.openapi.OpenApiIndexDataSyncResultDto;
import com.sb10findexteam6.service.openapi.IndexDataOpenApiSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class IndexDataOpenApiSyncController {

    private final IndexDataOpenApiSyncService indexDataOpenApiSyncService;

    @GetMapping("/test/open-api/index-data-sync")
    public OpenApiIndexDataSyncResultDto syncIndexData(
            @RequestParam String targetDate,
            @RequestParam(defaultValue = "100") int numOfRows,
            @RequestParam(defaultValue = "1") int pageNo
    ) {
        return indexDataOpenApiSyncService.sync(targetDate, numOfRows, pageNo);
    }
}