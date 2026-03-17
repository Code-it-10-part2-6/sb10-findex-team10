package com.sb10findexteam6.dto.syncJob;

import com.sb10findexteam6.common.enums.JobType;
import com.sb10findexteam6.common.enums.Result;
import java.time.LocalDate;

public record SyncJobSearchCondition(
        JobType jobType,
        Long indexInfoId,
        LocalDate targetDateFrom,
        LocalDate targetDateTo,
        String worker,
        Result result,
        Long idAfter,
        String cursor,
        String sortField,
        String sortDirection,
        Integer size
) {
}
