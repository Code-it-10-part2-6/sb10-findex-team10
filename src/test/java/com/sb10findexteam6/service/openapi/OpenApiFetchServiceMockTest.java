package com.sb10findexteam6.service.openapi;

import com.sb10findexteam6.common.config.properties.OpenApiProperties;
import com.sb10findexteam6.common.exception.BusinessException;
import com.sb10findexteam6.dto.openapi.FscIndexResponseDto;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OpenAPI 통신 계층 테스트 (MockWebServer) - 공식 명세서 기준")
class OpenApiFetchServiceMockTest {

    private MockWebServer mockWebServer;
    private OpenApiFetchService openApiFetchService;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        OpenApiProperties properties = new OpenApiProperties();
        // 가짜 서버의 URL 주입
        properties.setBaseUrl(mockWebServer.url("/").toString());
        properties.setServiceKey("test-service-key");

        WebClient webClient = WebClient.builder()
            .baseUrl(properties.getBaseUrl())
            .build();

        openApiFetchService = new OpenApiFetchService(webClient, properties);
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    @DisplayName("정상 응답 시 공식 API 가이드의 필드가 DTO로 완벽하게 파싱된다.")
    void fetchStockMarketIndex_Success() {
        // given: 오픈API 활용자가이드의 "주가지수시세" 샘플 데이터를 바탕으로 한 JSON 응답 세팅
        String mockJsonResponse = """
            {
              "response": {
                "header": {
                  "resultCode": "00",
                  "resultMsg": "NORMAL SERVICE."
                },
                "body": {
                  "numOfRows": 1,
                  "pageNo": 1,
                  "totalCount": 1,
                  "items": {
                    "item": [
                      {
                        "basDt": "20240731",
                        "idxNm": "코스피",
                        "idxCsf": "KOSPI시리즈",
                        "epyItmsCnt": "839",
                        "clpr": "2770.69",
                        "vs": "32.5",
                        "fltRt": "1.19",
                        "mkp": "2745.58",
                        "hipr": "2770.7",
                        "lopr": "2733.63",
                        "trqu": "557090057",
                        "trPrc": "12197991898146",
                        "lstgMrktTotAmt": "2262832341048634",
                        "basPntm": "19800104",
                        "basIdx": "100"
                      }
                    ]
                  }
                }
              }
            }
            """;

        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody(mockJsonResponse));

        // when: 서비스 호출
        FscIndexResponseDto responseDto = openApiFetchService.fetchStockMarketIndex("20240731", 1, 1);

        // then: DTO 파싱 검증 (가이드 문서의 타입과 매핑이 잘 되었는지 확인)
        assertThat(responseDto).isNotNull();
        assertThat(responseDto.response().header().resultCode()).isEqualTo("00"); // 결과코드 확인

        FscIndexResponseDto.Item item = responseDto.response().body().items().item().get(0);

        // 지수 정보 (IndexInfo) 속성 검증
        assertThat(item.idxCsf()).isEqualTo("KOSPI시리즈"); // 지수분류명
        assertThat(item.idxNm()).isEqualTo("코스피"); // 지수명
        assertThat(item.epyItmsCnt()).isEqualTo("839"); // 채용종목 수
        assertThat(item.basPntm()).isEqualTo("19800104"); // 기준시점
        assertThat(item.basIdx()).isEqualTo("100"); // 기준지수

        // 지수 데이터 (IndexData) 속성 검증
        assertThat(item.basDt()).isEqualTo("20240731"); // 기준일자
        assertThat(item.mkp()).isEqualTo("2745.58"); // 시가
        assertThat(item.clpr()).isEqualTo("2770.69"); // 종가
        assertThat(item.hipr()).isEqualTo("2770.7"); // 고가
        assertThat(item.lopr()).isEqualTo("2733.63"); // 저가
        assertThat(item.vs()).isEqualTo("32.5"); // 대비
        assertThat(item.fltRt()).isEqualTo("1.19"); // 등락률
        assertThat(item.trqu()).isEqualTo("557090057"); // 거래량
        assertThat(item.trPrc()).isEqualTo("12197991898146"); // 거래대금
        assertThat(item.lstgMrktTotAmt()).isEqualTo("2262832341048634"); // 상장시가총액
    }

    @Test
    @DisplayName("API 에러 코드(예: 22, 서비스 요청제한횟수 초과) 응답 시 예외를 발생시킨다.")
    void fetchStockMarketIndex_Fail_OverLimit() {
        // given
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(500)
            .setBody("LIMITED_NUMBER_OF_SERVICE_REQUESTS_EXCEEDS_ERROR"));

        // when & then: BusinessException의 ErrorCode와 details를 정확하게 검증
        assertThatThrownBy(() -> openApiFetchService.fetchStockMarketIndex("20240731", 1, 1))
            .isInstanceOf(BusinessException.class)
            .satisfies(exception -> {
                BusinessException bizEx = (BusinessException) exception;
                // 1. 상태 코드가 OPEN_API_COMMUNICATION_ERROR 인지 검증
                assertThat(bizEx.getErrorCode().name()).isEqualTo("OPEN_API_COMMUNICATION_ERROR");
                // 2. details 필드에 "서버 응답 에러" 텍스트가 포함되어 있는지 검증
                assertThat(bizEx.getDetails()).contains("서버 응답 에러");
            });
    }
}