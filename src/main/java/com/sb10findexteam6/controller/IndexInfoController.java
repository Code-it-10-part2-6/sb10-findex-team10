package com.sb10findexteam6.controller;


import com.sb10findexteam6.dto.CursorPageResponseIndexInfoDto;
import com.sb10findexteam6.dto.indexinfo.IndexInfoCreateRequest;
import com.sb10findexteam6.dto.indexinfo.IndexInfoDto;
import com.sb10findexteam6.dto.indexinfo.IndexInfoSearchRequest;
import com.sb10findexteam6.dto.indexinfo.IndexInfoSummaryDto;
import com.sb10findexteam6.dto.indexinfo.IndexInfoUpdateRequest;
import com.sb10findexteam6.service.IndexInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "지수 정보 API", description = "지수 정보 관리 API")
@RestController
@RequestMapping("/api/index-infos")
@RequiredArgsConstructor
public class IndexInfoController {

  private final IndexInfoService indexInfoService;


  @Operation(summary = "지수 정보 조회", description = "ID로 지수 정보를 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "지수 정보 조회 성공",
          content = @Content(schema = @Schema(implementation = IndexInfoDto.class))),
      @ApiResponse(responseCode = "404", description = "조회할 지수 정보를 찾을 수 없음",
          content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
      @ApiResponse(responseCode = "500", description = "서버 오류",
          content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
  })
  @GetMapping("/{id}")
  public ResponseEntity<IndexInfoDto> findById(@PathVariable Long id) {
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(indexInfoService.findById(id));
  }

  @Operation(summary = "지수 정보 등록", description = "새로운 지수 정보를 등록합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "201", description = "지수 정보 생성 성공",
          content = @Content(schema = @Schema(implementation = IndexInfoDto.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (필수 필드 누락 등)",
          content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
      @ApiResponse(responseCode = "500", description = "서버 오류",
          content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
  })
  @PostMapping
  public ResponseEntity<IndexInfoDto> create(@RequestBody IndexInfoCreateRequest request) {
    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(indexInfoService.create(request));
  }

  @Operation(summary = "지수 정보 수정", description = "기존 지수 정보를 수정합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "지수 정보 수정 성공",
          content = @Content(schema = @Schema(implementation = IndexInfoDto.class))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 필드 값 등)",
          content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
      @ApiResponse(responseCode = "404", description = "수정할 지수 정보를 찾을 수 없음",
          content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
      @ApiResponse(responseCode = "500", description = "서버 오류",
          content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
  })
  @PatchMapping("/{id}")
  public ResponseEntity<IndexInfoDto> update(
      @PathVariable Long id,
      @RequestBody IndexInfoUpdateRequest request
  ) {
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(indexInfoService.update(id, request));
  }

  @Operation(summary = "지수 정보 삭제", description = "지수 정보를 삭제합니다. 관련된 지수 데이터도 함께 삭제됩니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "지수 정보 삭제 성공"),
      @ApiResponse(responseCode = "404", description = "삭제할 지수 정보를 찾을 수 없음",
          content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
      @ApiResponse(responseCode = "500", description = "서버 오류",
          content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    indexInfoService.delete(id);
    return ResponseEntity
        .status(HttpStatus.NO_CONTENT)
        .build();
  }


  @Operation(summary = "지수 정보 목록 조회", description = "지수 정보 목록을 조회합니다. 필터링, 정렬, 커서 기반 페이지네이션을 지원합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "지수 정보 목록 조회 성공",
          content = @Content(schema = @Schema(ref = "#/components/schemas/CursorPageResponseIndexInfoDto"))),
      @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효하지 않은 필터 값 등)",
          content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse"))),
      @ApiResponse(responseCode = "500", description = "서버 오류",
          content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
  })
  @GetMapping
  public ResponseEntity<CursorPageResponseIndexInfoDto> findIndexInfoList(
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

  @Operation(summary = "지수 정보 요약 목록 조회", description = "지수 ID, 분류, 이름만 포함한 전체 지수 목록을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "지수 정보 요약 목록 조회 성공",
          content = @Content(schema = @Schema(ref = "#/components/schemas/IndexInfoSummaryDto"))),
      @ApiResponse(responseCode = "500", description = "서버 오류",
          content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
  })
  @GetMapping("/summaries")
  public ResponseEntity<List<IndexInfoSummaryDto>> findSummaryList() {
    return ResponseEntity
        .status(HttpStatus.OK)
        .body(indexInfoService.findSummaryList());
  }
}
