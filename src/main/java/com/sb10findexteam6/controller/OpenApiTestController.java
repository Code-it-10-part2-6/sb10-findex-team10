package com.sb10findexteam6.controller;

import com.sb10findexteam6.dto.openapi.FscIndexResponseDto;
import com.sb10findexteam6.service.openapi.OpenApiFetchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OpenApiTestController {

    private final OpenApiFetchService openApiFetchService;

    @GetMapping("/test/open-api")
    public FscIndexResponseDto testOpenApi(
            @RequestParam String targetDate,
            @RequestParam(defaultValue = "5") int numOfRows,
            @RequestParam(defaultValue = "1") int pageNo
    ) {
        return openApiFetchService.fetchStockMarketIndex(targetDate, numOfRows, pageNo);
    }
}