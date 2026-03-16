package com.sb10findexteam6.mapper;


import com.sb10findexteam6.dto.indexinfo.IndexInfoDto;
import com.sb10findexteam6.dto.indexinfo.IndexInfoSummaryDto;
import com.sb10findexteam6.entity.IndexInfo;
import org.springframework.stereotype.Component;

@Component
public class IndexInfoMapper {

  public IndexInfoDto toDto(IndexInfo indexInfo){
    return new IndexInfoDto(
        indexInfo.getId(),
        indexInfo.getIndexClassification(),
        indexInfo.getIndexName(),
        indexInfo.getEmployedItemsCount(),
        indexInfo.getBasePointInTime(),
        indexInfo.getBaseIndex(),
        indexInfo.getSourceType().name(),
        indexInfo.isFavorite()
        );
  }

  public IndexInfoSummaryDto toSummaryDto(IndexInfo indexInfo) {
    return new IndexInfoSummaryDto(
        indexInfo.getId(),
        indexInfo.getIndexClassification(),
        indexInfo.getIndexName()
    );
  }


}
