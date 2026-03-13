package com.sb10findexteam6.common.paging.dto;

import java.util.List;

public record PagingResponse<T>(
    List<T> content,
    Long nextCursor,
    boolean hasNext
) {

}
