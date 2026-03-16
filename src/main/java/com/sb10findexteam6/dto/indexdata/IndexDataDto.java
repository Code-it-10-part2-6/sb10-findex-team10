package com.sb10findexteam6.dto.indexdata;
// 지수 데이터 응답에 사용하는 DTO.
import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexDataDto(
        Long id,
        Long indexInfoId,
        LocalDate baseDate,
        String sourceType,
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
