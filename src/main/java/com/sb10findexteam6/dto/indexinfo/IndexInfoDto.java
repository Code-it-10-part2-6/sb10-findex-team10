package com.sb10findexteam6.dto.indexinfo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IndexInfoDto(
    Long id,
    String indexClassification,
    String indexName,
    int employedItemsCount,
    LocalDate basePointInTime,
    BigDecimal baseIndex,
    String sourceType,
    boolean favorite

) {

}
