package com.sb10findexteam6.controller;

import com.sb10findexteam6.dto.CursorPageResponse;
import com.sb10findexteam6.dto.indexdata.IndexDataCreateRequest;
import com.sb10findexteam6.dto.indexdata.IndexDataDto;
import com.sb10findexteam6.dto.indexdata.IndexDataSearchCondition;
import com.sb10findexteam6.dto.indexdata.IndexDataUpdateRequest;
import com.sb10findexteam6.service.IndexDataService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "지수 데이터 API", description = "지수 데이터 관리 API")
@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexDataController {
  private final IndexDataService indexDataService;

  @Operation(summary = "지수 데이터 목록 조회")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public IndexDataDto create(@RequestBody IndexDataCreateRequest request) {
    return indexDataService.create(request);
  }

  @Operation(summary = "지수 데이터 수정")
  @PatchMapping("/{id}")
  public IndexDataDto update(@PathVariable Long id, @RequestBody IndexDataUpdateRequest request) {
    return indexDataService.update(id, request);
  }

  @Operation(summary = ".지수 데이터 삭제")
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    indexDataService.delete(id);
  }

  @Hidden
  @GetMapping("/{id}")
  public IndexDataDto getById(@PathVariable Long id) {
    return indexDataService.getById(id);
  }

  //목록 조회(필터링, 페이지네이션)
  @Operation(summary = "지수 데이터 목록 조회")
  @GetMapping
  public CursorPageResponse<IndexDataDto> getAll(
      @RequestParam(required = false) Long indexInfoId,
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate,
      @RequestParam(required = false) Long idAfter,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false, defaultValue = "baseDate") String sortField,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(required = false, defaultValue = "10") Integer size
  ) {
    IndexDataSearchCondition condition = new IndexDataSearchCondition();
    condition.setIndexInfoId(indexInfoId);
    condition.setStartDate(startDate);
    condition.setEndDate(endDate);
    condition.setIdAfter(idAfter);
    condition.setCursor(cursor);
    condition.setSortField(sortField);
    condition.setSortDirection(sortDirection);
    condition.setSize(size);

    return indexDataService.getAll(condition);
  }
  // 지수 차트 조회
  // 지수 성과 랭킹 조회
  // 관심 지수 성과 조회

  // CSV 파일 Export
  @GetMapping(value = "/export/csv")
  // CSV 파일 Export\
  @Operation(summary = "지수 데이터 CSV export")
  @GetMapping(value = "/export/csv")
  public ResponseEntity<byte[]> export(
          @RequestParam(required = false) Long indexInfoId,
          @RequestParam(required = false) LocalDate startDate,
          @RequestParam(required = false) LocalDate endDate,
          @RequestParam(required = false, defaultValue = "baseDate") String sortField,
          @RequestParam(required = false, defaultValue = "desc") String sortDirection
  ) {
    IndexDataSearchCondition condition = new IndexDataSearchCondition();
    condition.setIndexInfoId(indexInfoId);
    condition.setStartDate(startDate);
    condition.setEndDate(endDate);
    condition.setSortField(sortField);
    condition.setSortDirection(sortDirection);

    byte[] csv = indexDataService.export(condition);

    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"index-data.csv\"")
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(csv);
  }
}
