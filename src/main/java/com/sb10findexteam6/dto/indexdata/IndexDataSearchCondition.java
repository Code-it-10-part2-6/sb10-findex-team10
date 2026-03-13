package com.sb10findexteam6.dto.indexdata;

import lombok.Getter;
import java.time.LocalDate;

// 목록 조회용
// IndexData 조회 시 필요 조건 값들 집합.
@Getter
public class IndexDataSearchCondition {
    private Long indexInfoId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long idAfter;
    private String cursor;
    private String sortField;
    private String sortDirection;
    private Integer size;
}
