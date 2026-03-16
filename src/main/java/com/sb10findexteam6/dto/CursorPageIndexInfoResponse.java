package com.sb10findexteam6.dto;

import java.util.List;

public record CursorPageIndexInfoResponse<T>(
    List<T> content,
    String nextCursor,
    Long nextIdAfter,
    int size,
    long totalElements,
    boolean hasNext) {

}
