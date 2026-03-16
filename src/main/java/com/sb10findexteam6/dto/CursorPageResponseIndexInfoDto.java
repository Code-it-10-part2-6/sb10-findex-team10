package com.sb10findexteam6.dto;

import com.sb10findexteam6.dto.indexinfo.IndexInfoDto;
import java.util.List;

public record CursorPageResponseIndexInfoDto(
    List<IndexInfoDto> content,
    String nextCursor,
    Long nextIdAfter,
    int size,
    long totalElements,
    boolean hasNext
) {}