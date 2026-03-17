package com.sb10findexteam6.dto.dashboard;

import java.time.LocalDate;

// 차트 기간 유형
public enum PeriodType {
  DAILY,
  WEEKLY,
  MONTHLY,
  QUARTERLY,
  YEARLY;

  public LocalDate getStartDate(LocalDate today) {
    return switch (this) {
      case DAILY -> today;
      case WEEKLY -> today.minusDays(7);
      case MONTHLY -> today.minusMonths(1);
      case QUARTERLY -> today.minusMonths(3);
      case YEARLY -> today.minusYears(1);
    };
  }
}
