package com.sb10findexteam6.controller;

import com.sb10findexteam6.dto.PagingResponse;
import com.sb10findexteam6.dto.indexdata.IndexDataCreateRequest;
import com.sb10findexteam6.dto.indexdata.IndexDataDto;
import com.sb10findexteam6.dto.indexdata.IndexDataSearchCondition;
import com.sb10findexteam6.dto.indexdata.IndexDataUpdateRequest;
import com.sb10findexteam6.service.IndexDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

  @PatchMapping("{id}")
  public IndexDataDto update(@PathVariable Long id, @RequestBody IndexDataUpdateRequest request) {
    return indexDataService.update(id, request);
  }

  @DeleteMapping("{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    indexDataService.delete(id);
  }

  @GetMapping("/{id}")
  public IndexDataDto getById(@PathVariable Long id) {
    return indexDataService.getById(id);
  }

  @GetMapping
  public PagingResponse<IndexDataDto> getAll(
      @RequestParam(required = false) Long indexInfoId,
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate,
      @RequestParam(required = false) Long idAfter,
      @RequestParam(required = false) String cursor,
      @RequestParam(required = false, defaultValue = "baseDate") String sortField,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(required = false, defaultValue = "10") Integer size) {
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
}
