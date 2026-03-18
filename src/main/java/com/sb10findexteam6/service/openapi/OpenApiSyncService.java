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

        return fetchTargetItemWithPaging(targetDateStr, indexInfo.getIndexName())
            .map(item -> openApiDataMapper.mapToEntity(item, indexInfo));
    }

    /**
     * API 응답의 전체 페이지를 순회하며 우리가 원하는 지수명만 정확히 필터링해서 추출
     */
    private Optional<FscIndexResponseDto.Item> fetchTargetItemWithPaging(String targetDateStr, String targetIndexName) {
        int pageNo = 1;
        int numOfRows = 100; // 한 페이지당 요청 건수

        while (true) {
            // API 호출
            FscIndexResponseDto responseDto = openApiFetchService.fetchStockMarketIndex(targetDateStr, numOfRows, pageNo);

            // 응답 포맷이 비정상일 경우 종료
            if (responseDto == null || responseDto.response() == null || responseDto.response().body() == null) {
                break;
            }

            FscIndexResponseDto.Body body = responseDto.response().body();

            // 현재 페이지의 데이터 목록에서 타겟 지수명이 있는지 검사
            if (body.items() != null && body.items().item() != null) {
                Optional<FscIndexResponseDto.Item> foundItem = body.items().item().stream()
                    .filter(item -> item.idxNm().equals(targetIndexName))
                    .findFirst();

                // 타겟 지수를 찾았다면 바로 반환 (탐색 종료)
                if (foundItem.isPresent()) {
                    return foundItem;
                }
            }

            // 현재 페이지에 타겟 지수가 없다면, 다음 페이지가 있는지 검사
            int totalCount = body.totalCount();

            // 더 이상 조회할 데이터가 없거나, 전체 개수가 0이면 탐색 종료
            if (pageNo * numOfRows >= totalCount || totalCount == 0) {
                break;
            }
            // 다음 페이지로 이동
            pageNo++;
        }

        // 끝까지 뒤졌지만 찾지 못한 경우 (휴장일 등)
        return Optional.empty();
    }
}