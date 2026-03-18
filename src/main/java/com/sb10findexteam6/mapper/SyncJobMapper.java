package com.sb10findexteam6.mapper;

import com.sb10findexteam6.dto.syncJob.SyncJobDto;
import com.sb10findexteam6.entity.SyncJob;

import java.util.List;

public class SyncJobMapper {

    private SyncJobMapper() {}

    public static SyncJobDto toDto(SyncJob syncJob) {
        if (syncJob == null)
            return null;

        return new SyncJobDto(
                syncJob.getId(),
                syncJob.getJobType(),
                syncJob.getIndexInfo() != null ? syncJob.getIndexInfo().getId() : null,
                syncJob.getTargetDate(),
                syncJob.getWorker(),
                syncJob.getJobTime(),
                syncJob.getResult()
        );
    }

    public static List<SyncJobDto> toDtoList(List<SyncJob> syncJobs) {
        if (syncJobs == null)
            return List.of();

        return syncJobs.stream()
                .map(SyncJobMapper::toDto)
                .toList();
    }
}
