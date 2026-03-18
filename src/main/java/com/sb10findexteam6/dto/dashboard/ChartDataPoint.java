package com.sb10findexteam6.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ChartDataPoint(
    LocalDate date,
    BigDecimal value
) {

}
