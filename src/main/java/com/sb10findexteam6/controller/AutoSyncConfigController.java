package com.sb10findexteam6.controller;

import com.sb10findexteam6.common.exception.ErrorResponse;
import com.sb10findexteam6.dto.autosyncconfig.AutoSyncConfigDto;
import com.sb10findexteam6.dto.autosyncconfig.AutoSyncConfigUpdateRequest;
import com.sb10findexteam6.service.AutoSyncConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auto-sync-configs")
@Tag(name = "자동 연동 설정 API", description = "자동 연동 설정 API")
public class AutoSyncConfigController {

  private final AutoSyncConfigService autoSyncConfigService;

  @Operation(summary = "자동 연동 설정 수정", description = "기존 자동 연동 설정을 수정합니다.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200", description = "자동 연동 설정 수정 성공",
          content = @Content(
              mediaType = MediaType.ALL_VALUE,
              schema = @Schema(implementation = AutoSyncConfigDto.class)
          )
      ),
      @ApiResponse(
          responseCode = "400", description = "잘못된 요청 (유효하지 않은 설정 값 등)",
          content = @Content(
              mediaType = MediaType.ALL_VALUE,
              schema = @Schema(implementation = ErrorResponse.class)
          )
      ),
      @ApiResponse(
          responseCode = "500", description = "서버 오류",
          content = @Content(
              mediaType = MediaType.ALL_VALUE,
              schema = @Schema(implementation = ErrorResponse.class)
          )
      ),
      @ApiResponse(
          responseCode = "404", description = "수정할 자동 연동 설정을 찾을 수 없음",
          content = @Content(
              mediaType = MediaType.ALL_VALUE,
              schema = @Schema(implementation = ErrorResponse.class)
          )
      )
  })
  @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<AutoSyncConfigDto> update(
      @Parameter(description = "자동 연동 설정 ID", required = true) @PathVariable Long id,
      @RequestBody AutoSyncConfigUpdateRequest request
  ) {
    return ResponseEntity.ok(autoSyncConfigService.update(id, request));
  }

  @Operation(summary = "자동 연동 설정 목록 조회", description = "자동 연동 설정 목록을 조회합니다. 필터링, 정렬, 커서 기반 페이지네이션을 지원합니다.")
  @ApiResponses(value = {
      @ApiResponse(
          responseCode = "200", description = "자동 연동 설정 목록 조회 성공",
          content = @Content(
              mediaType = MediaType.ALL_VALUE,
              array = @ArraySchema(schema = @Schema(implementation = AutoSyncConfigDto.class)) // 페이징 시 수정
          )
      ),
      @ApiResponse(
          responseCode = "400", description = "잘못된 요청 (유효하지 않은 필터 값 등)",
          content = @Content(
              mediaType = MediaType.ALL_VALUE,
              schema = @Schema(implementation = ErrorResponse.class)
          )
      ),
      @ApiResponse(
          responseCode = "500", description = "서버 오류",
          content = @Content(
              mediaType = MediaType.ALL_VALUE,
              schema = @Schema(implementation = ErrorResponse.class)
          )
      ),
  })
  @GetMapping
  public ResponseEntity<List<AutoSyncConfigDto>> getAll() {
    // 페이징 시 수정
    return ResponseEntity.ok(autoSyncConfigService.getAll());
  }
}
