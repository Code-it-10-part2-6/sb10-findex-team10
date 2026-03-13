package com.sb10findexteam6.dto.indexdata;
// 지수 데이터 생성 요청 시 사용하는 DTO
import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexDataCreateRequest(
        Long indexInfoId,
        LocalDate baseDate,
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
