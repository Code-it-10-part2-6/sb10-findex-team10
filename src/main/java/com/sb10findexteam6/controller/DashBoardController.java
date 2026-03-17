package com.sb10findexteam6.controller;

import com.sb10findexteam6.dto.dashboard.*;
import com.sb10findexteam6.service.DashBoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "지수 데이터 API", description = "지수 데이터 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping
public class DashBoardController {

    private final DashBoardService dashBoardService;

    /**
     * 주요 지수 현황 요약 (즐겨찾기 지수 최신 종가/대비/등락률 리스트)
     */
    @Operation(summary = "관심 지수 성과 조회")
    @GetMapping("/api/index-data/performance/favorite")
    public ResponseEntity<List<IndexPerformanceDto>> getFavoriteIndexesPerformance() {
        List<IndexPerformanceDto> result = dashBoardService.getFavoriteIndexesPerformance();
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "지수 차트 조회")
    @GetMapping("/api/index-data/{id}/chart")
    public ResponseEntity<IndexChartDto> getIndexChart(
        @PathVariable Long id,
        @RequestParam(defaultValue = "DAILY") PeriodType periodType
    ) {
      return ResponseEntity.ok(dashBoardService.getChartIndex(id, periodType));
    }

     // 지수 성과 랭킹 조회
     @Operation(summary = "지수 성과 랭킹 조회")
     @GetMapping("/api/index-data/performance/rank")
     public ResponseEntity<List<RankedIndexPerformanceDto>> getPerformanceRank(
             @RequestParam(required = false) Long indexInfoId,
             @RequestParam(defaultValue = "DAILY") PeriodType periodType,
             @RequestParam(defaultValue = "10") int limit
     ) {
         return ResponseEntity.ok(
                 dashBoardService.getPerformanceRank(indexInfoId, periodType, limit)
         );
     }
}
