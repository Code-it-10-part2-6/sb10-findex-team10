package com.sb10findexteam6.mapper;

import com.sb10findexteam6.dto.autosyncconfig.AutoSyncConfigDto;
import com.sb10findexteam6.dto.autosyncconfig.AutoSyncConfigUpdateRequest;
import com.sb10findexteam6.entity.AutoSyncConfig;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface AutoSyncConfigMapper {

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void update(AutoSyncConfigUpdateRequest request, @MappingTarget AutoSyncConfig config);

  @Mapping(target = "indexInfoId", source = "indexInfo.id")
  @Mapping(target = "indexClassification", source = "indexInfo.indexClassification")
  @Mapping(target = "indexName", source = "indexInfo.indexName")
  AutoSyncConfigDto toDto(AutoSyncConfig config);
}
