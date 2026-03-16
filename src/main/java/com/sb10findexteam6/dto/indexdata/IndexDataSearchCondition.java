package com.sb10findexteam6.dto.indexdata;
// 목록 조회용
// IndexData 조회 시 필요 조건 값들 집합.
// 조건 1개 ~ 8개 선택적
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
@Getter
@Setter
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
