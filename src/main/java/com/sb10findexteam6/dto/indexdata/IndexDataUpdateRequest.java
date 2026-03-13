package com.sb10findexteam6.dto.indexdata;
// 지수 데이터 수정 요청 시 사용하는 DTO (지수 정보 ID와 날짜 제외)
import java.math.BigDecimal;

public record IndexDataUpdateRequest(
        BigDecimal marketPrice,
        BigDecimal closingPrice,
        BigDecimal highPrice,
        BigDecimal lowPrice,
        BigDecimal versus,
        BigDecimal fluctuationRate,
        Long tradingQuantity,
        Long tradingPrice,
        Long marketTotalAmount
) {}
