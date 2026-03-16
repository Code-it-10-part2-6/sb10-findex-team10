package com.sb10findexteam6.service;

import com.sb10findexteam6.common.exception.BusinessException;
import com.sb10findexteam6.common.exception.ErrorCode;
import com.sb10findexteam6.dto.CursorPageResponse;
import com.sb10findexteam6.dto.autosyncconfig.AutoSyncConfigDto;
import com.sb10findexteam6.dto.autosyncconfig.AutoSyncConfigSearchCondition;
import com.sb10findexteam6.dto.autosyncconfig.AutoSyncConfigUpdateRequest;
import com.sb10findexteam6.entity.AutoSyncConfig;
import com.sb10findexteam6.entity.IndexInfo;
import com.sb10findexteam6.mapper.AutoSyncConfigMapper;
import com.sb10findexteam6.mapper.PagingMapper;
import com.sb10findexteam6.repository.AutoSyncConfigRepository;
import com.sb10findexteam6.repository.AutoSyncConfigRepositoryCustom.AutoSyncConfigSearchResult;
import com.sb10findexteam6.repository.IndexInfoRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AutoSyncConfigServiceImpl implements AutoSyncConfigService{

  private final AutoSyncConfigRepository autoSyncConfigRepository;
  private final IndexInfoRepository indexInfoRepository;
  private final AutoSyncConfigMapper autoSyncConfigMapper;
  private final PagingMapper pagingMapper;

  @Override
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

  @Override
  @Transactional(readOnly = true)
  public AutoSyncConfigDto getById(Long id) {
    return autoSyncConfigMapper.toDto(autoSyncConfigRepository.findById(id).orElseThrow(
        () -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR) // 추후 수정
    ));
  }

  @Override
  @Transactional(readOnly = true)
  public CursorPageResponse<AutoSyncConfigDto> getAll(AutoSyncConfigSearchCondition condition) {
    Long resolvedIdAfter = pagingMapper.resolveIdAfter(
        condition.getCursor(),
        condition.getIdAfter()
    );
    condition.setIdAfter(resolvedIdAfter);

    AutoSyncConfigSearchResult result = autoSyncConfigRepository.search(condition);

    List<AutoSyncConfigDto> dtoList = result.content().stream()
        .map(autoSyncConfigMapper::toDto)
        .toList();

    int size = condition.getSize() != null ? condition.getSize() : 10;

    return pagingMapper.toResponse(
        dtoList,
        size,
        result.totalCount(),
        AutoSyncConfigDto::id
    );
  }

  @Override
  public AutoSyncConfigDto update(Long id, AutoSyncConfigUpdateRequest request) {
    AutoSyncConfig autoSyncConfig = autoSyncConfigRepository.findById(id).orElseThrow(
        () -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR) // 추후 수정
    );
    autoSyncConfigMapper.update(request, autoSyncConfig);
    return autoSyncConfigMapper.toDto(autoSyncConfig);
  }
}
