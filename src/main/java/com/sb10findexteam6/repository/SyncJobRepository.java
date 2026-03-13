package com.sb10findexteam6.repository;

import com.sb10findexteam6.entity.SyncJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SyncJobRepository extends JpaRepository<SyncJob, Long>, JpaSpecificationExecutor<SyncJob> {
}
