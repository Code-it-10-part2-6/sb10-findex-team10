package com.sb10findexteam6.dto.syncJob;

import java.util.List;

public record CursorPageResponseSyncJobDto(
        List<SyncJobDto> content,
        String nextCursor,
        Long nextIdAfter,
        Integer size,
        Long totalElements,
        Boolean hasNext
) {}
