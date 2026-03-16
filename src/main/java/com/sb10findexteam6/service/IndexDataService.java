package com.sb10findexteam6.service;

import com.sb10findexteam6.dto.indexdata.IndexDataCreateRequest;
import com.sb10findexteam6.dto.indexdata.IndexDataDto;
import com.sb10findexteam6.dto.indexdata.IndexDataSearchCondition;
import com.sb10findexteam6.dto.indexdata.IndexDataUpdateRequest;

public interface IndexDataService {
    IndexDataDto create (IndexDataCreateRequest request);

    IndexDataDto update (Long id, IndexDataUpdateRequest request);

    void delete (Long id);

    IndexDataDto getById(Long id);
    // 목록 조회는 나중에 구현
    //PagingResponse<IndexDataDto> getAll(IndexDataSearchCondition condition);
}
