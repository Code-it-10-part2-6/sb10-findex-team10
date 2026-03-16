package com.sb10findexteam6.dto;

import java.util.List;

public record PagingResponse<T>(
    List<T> content,
    Long nextCursor,
    Long nextIdAfter,
    int size,
    long totalElements,
    boolean hasNext
) {

}
