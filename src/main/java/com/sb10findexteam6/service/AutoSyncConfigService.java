package com.sb10findexteam6.service;

import com.sb10findexteam6.dto.autosyncconfig.AutoSyncConfigDto;
import com.sb10findexteam6.dto.autosyncconfig.AutoSyncConfigUpdateRequest;
import java.util.List;

public interface AutoSyncConfigService {

  AutoSyncConfigDto create(Long indexInfoId);
  AutoSyncConfigDto getById(Long id);
  List<AutoSyncConfigDto> getAll();
  AutoSyncConfigDto update(Long id, AutoSyncConfigUpdateRequest request);
}
