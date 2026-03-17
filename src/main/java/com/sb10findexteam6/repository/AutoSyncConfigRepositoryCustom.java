package com.sb10findexteam6.repository;

import com.sb10findexteam6.dto.autosyncconfig.AutoSyncConfigSearchCondition;
import com.sb10findexteam6.entity.AutoSyncConfig;
import java.util.List;

public interface AutoSyncConfigRepositoryCustom {

  AutoSyncConfigSearchResult search(AutoSyncConfigSearchCondition condition);

  record AutoSyncConfigSearchResult(
      List<AutoSyncConfig> content,
      long totalCount
  ) {}
}
