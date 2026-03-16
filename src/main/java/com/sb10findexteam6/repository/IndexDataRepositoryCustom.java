package com.sb10findexteam6.repository;

import com.sb10findexteam6.dto.indexdata.IndexDataSearchCondition;
import com.sb10findexteam6.entity.IndexData;

import java.util.List;

public interface IndexDataRepositoryCustom {
  List<IndexData> search(IndexDataSearchCondition condition);
}
