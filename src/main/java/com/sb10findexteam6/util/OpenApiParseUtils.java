package com.sb10findexteam6.util;
// OpenApi 응답값이 문자열이라서 파싱 유틸 사용
import com.sb10findexteam6.common.exception.BusinessException;
import com.sb10findexteam6.common.exception.ErrorCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public final class OpenApiParseUtils {

    private static final DateTimeFormatter BASIC_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private OpenApiParseUtils() {
    }

    public static LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value, BASIC_DATE_FORMAT);
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "날짜 형식이 올바르지 않습니다. value= " + value
            );
        }
    }

    public static BigDecimal parseBigDecimal(String value) {
        try {
            if (value == null || value.isBlank()) {
                return BigDecimal.ZERO;
            }

            String normalized = value.trim();

            if (normalized.startsWith(".")) {
                normalized = "0" + normalized;
            } else if (normalized.startsWith("-.")) {
                normalized = normalized.replace("-.", "-0.");
            }

            return new BigDecimal(normalized);
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "숫자 형식이 올바르지 않습니다. value= " + value
            );
        }
    }

    public static Long parseLong(String value) {
        try {
            if (value == null || value.isBlank()) {
                return 0L;
            }
            return Long.parseLong(value.trim());
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "정수 형식이 올바르지 않습니다. value= " + value
            );
        }
    }
}