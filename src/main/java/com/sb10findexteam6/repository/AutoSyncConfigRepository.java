package com.sb10findexteam6.repository;

import com.sb10findexteam6.entity.AutoSyncConfig;
import com.sb10findexteam6.entity.IndexInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AutoSyncConfigRepository extends JpaRepository<AutoSyncConfig, Long> {
  void deleteByIndexInfo(IndexInfo indexInfo);
}
