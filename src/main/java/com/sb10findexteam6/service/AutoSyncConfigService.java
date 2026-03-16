package com.sb10findexteam6.service;

import com.sb10findexteam6.dto.CursorPageResponse;
import com.sb10findexteam6.dto.autosyncconfig.AutoSyncConfigDto;
import com.sb10findexteam6.dto.autosyncconfig.AutoSyncConfigSearchCondition;
import com.sb10findexteam6.dto.autosyncconfig.AutoSyncConfigUpdateRequest;

public interface AutoSyncConfigService {

  AutoSyncConfigDto create(Long indexInfoId);
  AutoSyncConfigDto getById(Long id);
  CursorPageResponse<AutoSyncConfigDto> getAll(AutoSyncConfigSearchCondition condition);
  AutoSyncConfigDto update(Long id, AutoSyncConfigUpdateRequest request);
}
