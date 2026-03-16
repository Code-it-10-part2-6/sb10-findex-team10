package com.sb10findexteam6.service;

import com.sb10findexteam6.common.exception.BusinessException;
import com.sb10findexteam6.common.exception.ErrorCode;
import com.sb10findexteam6.dto.autosyncconfig.AutoSyncConfigDto;
import com.sb10findexteam6.dto.autosyncconfig.AutoSyncConfigUpdateRequest;
import com.sb10findexteam6.entity.AutoSyncConfig;
import com.sb10findexteam6.entity.IndexInfo;
import com.sb10findexteam6.mapper.AutoSyncConfigMapper;
import com.sb10findexteam6.repository.AutoSyncConfigRepository;
import com.sb10findexteam6.repository.IndexInfoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AutoSyncConfigService {

  private final AutoSyncConfigRepository autoSyncConfigRepository;
  private final IndexInfoRepository indexInfoRepository;
  private final AutoSyncConfigMapper autoSyncConfigMapper;

  public AutoSyncConfigDto create(Long indexInfoId) {
    if(autoSyncConfigRepository.existsByIndexInfoId(indexInfoId)) {
      throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR); // 추후 수정
    }
    IndexInfo indexInfo = indexInfoRepository.findById(indexInfoId).orElseThrow(
        () -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR) // 추후 수정
    );
    AutoSyncConfig autoSyncConfig = new AutoSyncConfig(false, indexInfo);
    autoSyncConfigRepository.save(autoSyncConfig);
    return autoSyncConfigMapper.toDto(autoSyncConfig);
  }

  @Transactional(readOnly = true)
  public AutoSyncConfigDto getById(Long id) {
    return autoSyncConfigMapper.toDto(autoSyncConfigRepository.findById(id).orElseThrow(
        () -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR) // 추후 수정
    ));
  }

  @Transactional(readOnly = true)
  public List<AutoSyncConfigDto> getAll() {
    return autoSyncConfigRepository.findAll().stream()
        .map(autoSyncConfigMapper::toDto)
        .toList(); // 페이징 미적용
  }

  public AutoSyncConfigDto update(Long id, AutoSyncConfigUpdateRequest request) {
    AutoSyncConfig autoSyncConfig = autoSyncConfigRepository.findById(id).orElseThrow(
        () -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR) // 추후 수정
    );
    autoSyncConfigMapper.update(request, autoSyncConfig);
    return autoSyncConfigMapper.toDto(autoSyncConfig);
  }
}
