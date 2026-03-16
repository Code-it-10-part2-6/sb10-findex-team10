package com.sb10findexteam6.service.openapi;

import com.sb10findexteam6.common.config.properties.OpenApiProperties;
import com.sb10findexteam6.common.exception.BusinessException;
import com.sb10findexteam6.common.exception.ErrorCode;
import com.sb10findexteam6.dto.openapi.FscIndexResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
public class OpenApiFetchService {

  private final WebClient openApiWebClient;
  private final OpenApiProperties openApiProperties;

  public FscIndexResponseDto fetchStockMarketIndex(String targetDate, int numOfRows, int pageNo) {
    log.info("[OpenAPI 호출] 주가지수시세 요청: 일자={}, 페이지={}", targetDate, pageNo);

    URI uri =
        UriComponentsBuilder.fromHttpUrl(openApiProperties.getBaseUrl() + "/getStockMarketIndex")
            .queryParam("serviceKey", openApiProperties.getServiceKey()) // encoded key
            .queryParam("resultType", "json")
            .queryParam("numOfRows", numOfRows)
            .queryParam("pageNo", pageNo)
            .queryParam("basDt", targetDate)
            .build(true)
            .toUri();

    try {
      return openApiWebClient
          .get()
          .uri(uri)
          .retrieve()
          .bodyToMono(FscIndexResponseDto.class)
          .block();

    } catch (WebClientResponseException e) {
      log.error(
          "[OpenAPI 통신 에러] 상태 코드: {}, 응답 본문: {}", e.getStatusCode(), e.getResponseBodyAsString());
      throw new BusinessException(
          ErrorCode.OPEN_API_COMMUNICATION_ERROR, "서버 응답 에러: " + e.getStatusCode());
    } catch (Exception e) {
      log.error("[OpenAPI 알 수 없는 에러] 요청 처리 중 장애 발생", e);
      throw new BusinessException(
          ErrorCode.OPEN_API_COMMUNICATION_ERROR, "네트워크/파싱 예외: " + e.getMessage());
    }
  }
}
