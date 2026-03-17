package com.sb10findexteam6.controller;

import com.sb10findexteam6.dto.dashboard.IndexPerformanceDto;
import com.sb10findexteam6.service.DashBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashBoardController {

    private final DashBoardService dashBoardService;

    /**
     * 주요 지수 현황 요약 (즐겨찾기 지수 최신 종가/대비/등락률 리스트)
     */
    @GetMapping("/favorites")
    public ResponseEntity<List<IndexPerformanceDto>> getFavoriteIndexesPerformance() {
        List<IndexPerformanceDto> result = dashBoardService.getFavoriteIndexesPerformance();
        return ResponseEntity.ok(result);
    }
}