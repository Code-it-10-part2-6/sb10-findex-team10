package com.sb10findexteam6.dto.dashboard;

import java.math.BigDecimal;

public record IndexPerformanceRankDto(
        int rank,
        Long indexInfoId,
        String indexClassification,
        String indexName,
        BigDecimal versus,
        BigDecimal fluctuationRate,
        BigDecimal currentPrice,
        BigDecimal beforePrice
) {}
