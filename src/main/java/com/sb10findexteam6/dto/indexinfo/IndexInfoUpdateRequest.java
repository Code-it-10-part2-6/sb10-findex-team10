package com.sb10findexteam6.dto.indexinfo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexInfoUpdateRequest(
    int employedItemsCount,
    LocalDate basePointInTime,
    BigDecimal baseIndex,
    boolean favorite
) {

}
