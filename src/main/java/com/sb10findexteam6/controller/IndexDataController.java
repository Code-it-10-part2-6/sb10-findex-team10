package com.sb10findexteam6.controller;

import com.sb10findexteam6.dto.CursorPageResponse;
import com.sb10findexteam6.dto.indexdata.IndexDataCreateRequest;
import com.sb10findexteam6.dto.indexdata.IndexDataDto;
import com.sb10findexteam6.dto.indexdata.IndexDataSearchCondition;
import com.sb10findexteam6.dto.indexdata.IndexDataUpdateRequest;
import com.sb10findexteam6.service.IndexDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/index-data")
@RequiredArgsConstructor
public class IndexDataController {
  private final IndexDataService indexDataService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public IndexDataDto create(@RequestBody IndexDataCreateRequest request) {
    return indexDataService.create(request);
  }

  @PatchMapping("/{id}")
  public IndexDataDto update(@PathVariable Long id, @RequestBody IndexDataUpdateRequest request) {
    return indexDataService.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    indexDataService.delete(id);
  }

  @GetMapping("/{id}")
  public IndexDataDto getById(@PathVariable Long id) {
    return indexDataService.getById(id);
  }

  //목록 조회(필터링, 페이지네이션)
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
  @GetMapping(value = "/export", produces = "text/csv")
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
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=index-data.csv")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(csv);
  }
}
