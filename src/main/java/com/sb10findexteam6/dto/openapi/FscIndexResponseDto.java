package com.sb10findexteam6.dto.openapi;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 공공데이터포털 금융위원회 지수시세정보 OpenAPI 응답 매핑 DTO
 */
public record FscIndexResponseDto(
    @JsonProperty("response") Response response
) {
    public record Response(
        @JsonProperty("header") Header header,
        @JsonProperty("body") Body body
    ) {}

    public record Header(
        @JsonProperty("resultCode") String resultCode,
        @JsonProperty("resultMsg") String resultMsg
    ) {}

    public record Body(
        @JsonProperty("numOfRows") int numOfRows,
        @JsonProperty("pageNo") int pageNo,
        @JsonProperty("totalCount") int totalCount,
        @JsonProperty("items") Items items
    ) {}

    public record Items(
        @JsonProperty("item") List<Item> item
    ) {}

    public record Item(
        /* ==== 지수 정보 (IndexInfo) 속성 ==== */
        @JsonProperty("idxCsf") String idxCsf,          // 지수 분류명
        @JsonProperty("idxNm") String idxNm,            // 지수명
        @JsonProperty("epyItmsCnt") String epyItmsCnt,  // 채용 종목 수
        @JsonProperty("basPntm") String basPntm,        // 기준 시점
        @JsonProperty("basIdx") String basIdx,          // 기준 지수

        /* ==== 지수 데이터 (IndexData) 속성 ==== */
        @JsonProperty("basDt") String basDt,            // 기준 일자 (날짜)
        @JsonProperty("mkp") String mkp,                // 시가
        @JsonProperty("hipr") String hipr,              // 고가
        @JsonProperty("lopr") String lopr,              // 저가
        @JsonProperty("clpr") String clpr,              // 종가
        @JsonProperty("vs") String vs,                  // 대비
        @JsonProperty("fltRt") String fltRt,            // 등락률
        @JsonProperty("trqu") String trqu,              // 거래량
        @JsonProperty("trPrc") String trPrc,            // 거래대금
        @JsonProperty("lstgMrktTotAmt") String lstgMrktTotAmt // 상장 시가 총액
    ) {}
}