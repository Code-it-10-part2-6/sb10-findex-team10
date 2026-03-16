package com.sb10findexteam6.dto;

import com.sb10findexteam6.common.enums.JobType;
import com.sb10findexteam6.common.enums.Result;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SyncJobDto(
    Long id,
    JobType jobType,
    Long indexInfoId,
    LocalDate targetDate,
    String worker,
    LocalDateTime jobTime,
    Result result
) {}