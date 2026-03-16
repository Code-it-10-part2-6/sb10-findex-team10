package com.sb10findexteam6.controller;


import com.sb10findexteam6.dto.CursorPageIndexInfoResponse;
import com.sb10findexteam6.dto.IndexInfoCreateRequest;
import com.sb10findexteam6.dto.IndexInfoDto;
import com.sb10findexteam6.dto.IndexInfoSearchRequest;
import com.sb10findexteam6.dto.IndexInfoSummaryDto;
import com.sb10findexteam6.dto.IndexInfoUpdateRequest;
import com.sb10findexteam6.service.IndexInfoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/index-infos")
@RequiredArgsConstructor
public class IndexInfoController {

  private final IndexInfoService indexInfoService;

  @GetMapping("/{id}")
  public ResponseEntity<IndexInfoDto> findById(@PathVariable Long id) {
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(indexInfoService.findById(id));
  }

  @PostMapping
  public ResponseEntity<IndexInfoDto> create(@RequestBody IndexInfoCreateRequest request) {
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(indexInfoService.create(request));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<IndexInfoDto> update(
      @PathVariable Long id,
      @RequestBody IndexInfoUpdateRequest request
  ) {
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(indexInfoService.update(id, request));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    indexInfoService.delete(id);
    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }

  @GetMapping
  public ResponseEntity<CursorPageIndexInfoResponse<IndexInfoDto>> findIndexInfoList(
      @RequestParam(required = false) String indexClassification,
      @RequestParam(required = false) String indexName,
      @RequestParam(required = false) Boolean favorite,
      @RequestParam(required = false) Long idAfter,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "indexClassification") String sortField,
      @RequestParam(defaultValue = "asc") String sortDirection,
      @RequestParam(defaultValue = "10") int size
  ) {
    IndexInfoSearchRequest request = new IndexInfoSearchRequest(
        indexClassification, indexName, favorite,
        idAfter, cursor, sortField, sortDirection, size
    );
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(indexInfoService.findIndexInfoList(request));
  }

  @GetMapping("/summaries")
  public ResponseEntity<List<IndexInfoSummaryDto>> findSummaryList() {
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(indexInfoService.findSummaryList());
  }

}
