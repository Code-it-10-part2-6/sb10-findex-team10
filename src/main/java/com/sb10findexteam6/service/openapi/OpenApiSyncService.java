package com.sb10findexteam6.service.openapi;

import com.sb10findexteam6.dto.openapi.FscIndexResponseDto;
import com.sb10findexteam6.entity.IndexData;
import com.sb10findexteam6.entity.IndexInfo;
import com.sb10findexteam6.mapper.OpenApiDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenApiSyncService {

    private final OpenApiFetchService openApiFetchService;
    private final OpenApiDataMapper openApiDataMapper;

    private static final DateTimeFormatter API_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 특정 지수/일자에 대한 데이터 1건을 조회하여 IndexData로 변환
     */
    public Optional<IndexData> fetchOneDayIndexData(IndexInfo indexInfo, LocalDate targetDate) {
        String targetDateStr = targetDate.format(API_DATE_FORMAT);
        log.info("[OpenAPI 데이터 수집] 지수명: {}, 타겟 일자: {}", indexInfo.getIndexName(), targetDateStr);
        FscIndexResponseDto responseDto =
            openApiFetchService.fetchStockMarketIndex(targetDateStr, 100, 1);
        return extractTargetItem(responseDto, indexInfo.getIndexName())
            .map(item -> openApiDataMapper.mapToEntity(item, indexInfo));
    }

    /**
     * IndexInfo에 대해 startDate부터 endDate까지의 데이터를 수집
     */
    public List<IndexData> fetchIndexDataList(IndexInfo indexInfo, LocalDate startDate, LocalDate endDate) {
        List<IndexData> collectedData = new ArrayList<>();
        LocalDate currentDate = startDate;

        // 시작일부터 종료일까지 하루씩 증가하며 반복 호출
        while (!currentDate.isAfter(endDate)) {
            String targetDateStr = currentDate.format(API_DATE_FORMAT);
            log.info("[OpenAPI 데이터 수집] 지수명: {}, 타겟 일자: {}", indexInfo.getIndexName(), targetDateStr);

            // API 호출
            FscIndexResponseDto responseDto = openApiFetchService.fetchStockMarketIndex(targetDateStr, 100, 1);

            // 응답 데이터에서 타겟 지수와 일치하는 데이터 찾기 및 파싱
            extractTargetItem(responseDto, indexInfo.getIndexName())
                .map(item -> openApiDataMapper.mapToEntity(item, indexInfo))
                .ifPresentOrElse(
                    indexData -> {
                        collectedData.add(indexData);
                        log.debug("데이터 파싱 성공: 날짜={}, 종가={}", indexData.getBaseDate(), indexData.getClosingPrice());
                    },
                    () -> log.debug("해당 일자 데이터 없음 - 일자: {}", targetDateStr)
                );

            // 다음 날짜로 이동
            currentDate = currentDate.plusDays(1);
        }

        log.info("[OpenAPI 데이터 수집 완료] 지수명: {}, 수집된 데이터 개수: {}개", indexInfo.getIndexName(), collectedData.size());
        return collectedData;
    }

    /**
     * API 응답 리스트에서 우리가 원하는 지수명(예: "코스피")만 정확히 필터링해서 추출
     */
    private Optional<FscIndexResponseDto.Item> extractTargetItem(FscIndexResponseDto dto, String targetIndexName) {
        if (dto == null || dto.response() == null || dto.response().body() == null || dto.response().body().items() == null || dto.response().body().items().item() == null) {
            return Optional.empty();
        }

        return dto.response().body().items().item().stream()
            .filter(item -> item.idxNm().equals(targetIndexName))
            .findFirst();
    }
}