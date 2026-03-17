package com.sb10findexteam6.controller;

import com.sb10findexteam6.dto.dashboard.IndexPerformanceDto;
import com.sb10findexteam6.dto.dashboard.IndexChartDto;
import com.sb10findexteam6.dto.dashboard.IndexPerformanceRankDto;
import com.sb10findexteam6.dto.dashboard.PeriodType;
import com.sb10findexteam6.service.DashBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping
public class DashBoardController {

    private final DashBoardService dashBoardService;

    /**
     * 주요 지수 현황 요약 (즐겨찾기 지수 최신 종가/대비/등락률 리스트)
     */
    @GetMapping("/api/index-data/performance/favorite")
    public ResponseEntity<List<IndexPerformanceDto>> getFavoriteIndexesPerformance() {
        List<IndexPerformanceDto> result = dashBoardService.getFavoriteIndexesPerformance();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/index-data/{id}/chart")
    public ResponseEntity<IndexChartDto> getIndexChart(
        @PathVariable Long id,
        @RequestParam(defaultValue = "DAILY") PeriodType periodType
    ) {
      return ResponseEntity.ok(dashBoardService.getChartIndex(id, periodType));
    }

     // 지수 성과 랭킹 조회
    @GetMapping("/api/index-data/performance/rank")
    public ResponseEntity<List<IndexPerformanceRankDto>> getPerformanceRank(
            @RequestParam(required = false) Long indexInfoId,
            @RequestParam(defaultValue = "DAILY") PeriodType periodType,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(
                dashBoardService.getPerformanceRank(indexInfoId, periodType, limit)
        );
    }
}
