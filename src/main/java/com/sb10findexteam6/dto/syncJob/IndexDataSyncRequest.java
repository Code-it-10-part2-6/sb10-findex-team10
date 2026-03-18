package com.sb10findexteam6.dto.syncJob;

import java.time.LocalDate;
import java.util.List;

public record IndexDataSyncRequest(
        List<Long> indexInfoIds,
        LocalDate baseDateFrom,
        LocalDate baseDateTo
) {}
