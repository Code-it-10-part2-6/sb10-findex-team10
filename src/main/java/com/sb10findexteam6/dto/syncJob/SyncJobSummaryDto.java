package com.sb10findexteam6.dto.syncJob;

import java.time.LocalDateTime;

public record SyncJobSummaryDto(
        long successCountLast7Days,
        long failureCountLast7Days,
        LocalDateTime lastSyncTime
) {}
