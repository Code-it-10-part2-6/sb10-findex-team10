package com.sb10findexteam6.dto.indexdata;

import java.util.Arrays;

public enum IndexDataSortField {
    BASE_DATE("baseDate"),
    MARKET_PRICE("marketPrice"),
    CLOSING_PRICE("closingPrice"),
    HIGH_PRICE("highPrice"),
    LOW_PRICE("lowPrice"),
    VERSUS("versus"),
    FLUCTUATION_RATE("fluctuationRate"),
    TRADING_QUANTITY("tradingQuantity"),
    TRADING_PRICE("tradingPrice"),
    MARKET_TOTAL_AMOUNT("marketTotalAmount");

    private final String value;

    IndexDataSortField(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static IndexDataSortField from(String value) {
        return Arrays.stream(values())
                .filter(field -> field.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 정렬 필드입니다: " + value));
    }
}