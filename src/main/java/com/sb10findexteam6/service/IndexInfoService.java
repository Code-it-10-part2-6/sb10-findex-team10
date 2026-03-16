package com.sb10findexteam6.service;

import com.sb10findexteam6.dto.CursorPageIndexInfoResponse;
import com.sb10findexteam6.dto.IndexInfoCreateRequest;
import com.sb10findexteam6.dto.IndexInfoDto;
import com.sb10findexteam6.dto.IndexInfoSearchRequest;
import com.sb10findexteam6.dto.IndexInfoSummaryDto;
import com.sb10findexteam6.dto.IndexInfoUpdateRequest;
import java.util.List;

public interface IndexInfoService {
  IndexInfoDto create(IndexInfoCreateRequest request);
  IndexInfoDto update(Long id, IndexInfoUpdateRequest request);
  IndexInfoDto findById(Long id);
  void delete(Long id);
  CursorPageIndexInfoResponse<IndexInfoDto> findIndexInfoList(IndexInfoSearchRequest request) ;
  List<IndexInfoSummaryDto> findSummaryList();

}
