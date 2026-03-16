package com.sb10findexteam6.dto.indexdata;

// 지수 데이터 목록 조회에서 사용할 수 있는 SortField 값 정의 해놓은 enum.
import com.sb10findexteam6.common.exception.BusinessException;
import com.sb10findexteam6.common.exception.ErrorCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

public enum IndexDataSortField {
  BASE_DATE("baseDate", "baseDate", LocalDate.class),
  MARKET_PRICE("marketPrice", "marketPrice", BigDecimal.class),
  CLOSING_PRICE("closingPrice", "closingPrice", BigDecimal.class),
  HIGH_PRICE("highPrice", "highPrice", BigDecimal.class),
  LOW_PRICE("lowPrice", "lowPrice", BigDecimal.class),
  VERSUS("versus", "versus", BigDecimal.class),
  FLUCTUATION_RATE("fluctuationRate", "fluctuationRate", BigDecimal.class),
  TRADING_QUANTITY("tradingQuantity", "tradingQuantity", Long.class),
  TRADING_PRICE("tradingPrice", "tradingPrice", Long.class),
  MARKET_TOTAL_AMOUNT("marketTotalAmount", "marketTotalAmount", Long.class);

  private final String requestValue;
  private final String entityField;
  private final Class<?> valueType;

  IndexDataSortField(String requestValue, String entityField, Class<?> valueType) {
    this.requestValue = requestValue;
    this.entityField = entityField;
    this.valueType = valueType;
  }

  public String getRequestValue() {
    return requestValue;
  }

  public String getEntityField() {
    return entityField;
  }

  public Class<?> getValueType() {
    return valueType;
  }

  public static IndexDataSortField from(String value) {
    return Arrays.stream(values())
            .filter(field -> field.requestValue.equals(value))
            .findFirst()
            .orElseThrow(() -> new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "유효하지 않은 정렬 필드입니다: " + value
            ));
  }
}
