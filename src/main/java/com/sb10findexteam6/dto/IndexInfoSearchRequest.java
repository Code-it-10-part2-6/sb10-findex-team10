package com.sb10findexteam6.dto;

public record IndexInfoSearchRequest (
    String indexClassification,  // 지수 분류명
    String indexName,            // 지수명
    Boolean favorite,            // 즐겨찾기
    Long idAfter,                // 이전 페이지 마지막 요소 ID
    String cursor,               // 커서
    String sortField,            // 정렬 필드 (default: indexClassification)
    String sortDirection,        // 정렬 방향 (default: asc)
    int size                     // 페이지 크기 (default: 10)
){

}
