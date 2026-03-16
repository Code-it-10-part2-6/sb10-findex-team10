package com.sb10findexteam6.service;

import com.sb10findexteam6.dto.CursorPageResponseIndexInfoDto;
import com.sb10findexteam6.dto.indexinfo.IndexInfoCreateRequest;
import com.sb10findexteam6.dto.indexinfo.IndexInfoDto;
import com.sb10findexteam6.dto.indexinfo.IndexInfoSearchRequest;
import com.sb10findexteam6.dto.indexinfo.IndexInfoSummaryDto;
import com.sb10findexteam6.dto.indexinfo.IndexInfoUpdateRequest;
import java.util.List;

public interface IndexInfoService {
  IndexInfoDto create(IndexInfoCreateRequest request);
  IndexInfoDto update(Long id, IndexInfoUpdateRequest request);
  IndexInfoDto findById(Long id);
  void delete(Long id);
  CursorPageResponseIndexInfoDto findIndexInfoList(IndexInfoSearchRequest request) ;
  List<IndexInfoSummaryDto> findSummaryList();
  List<IndexInfoDto> createFromOpenApi(String targetDate);


}
