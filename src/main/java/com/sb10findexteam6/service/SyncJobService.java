package com.sb10findexteam6.service;

import com.sb10findexteam6.dto.syncJob.*;

import java.util.List;

public interface SyncJobService {

    CursorPageResponseSyncJobDto getAll(SyncJobSearchCondition condition);

    SyncJobSummaryDto getSummary();

    List<SyncJobDto> syncIndexInfos(String worker);

    List<SyncJobDto> syncIndexData(IndexDataSyncRequest request, String worker);
}
