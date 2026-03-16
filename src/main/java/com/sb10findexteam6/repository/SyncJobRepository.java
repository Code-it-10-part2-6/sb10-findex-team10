package com.sb10findexteam6.repository;

import com.sb10findexteam6.common.enums.JobType;
import com.sb10findexteam6.common.enums.Result;
import com.sb10findexteam6.entity.IndexInfo;
import com.sb10findexteam6.entity.SyncJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface SyncJobRepository extends JpaRepository<SyncJob, Long>, JpaSpecificationExecutor<SyncJob> {

    /**
     * 특정 지수(IndexInfo)에 대해, 특정 작업(JobType)이 성공한
     * 가장 최근의 대상 일자를 조회
     */
    @Query("SELECT MAX(s.targetDate) FROM SyncJob s " +
        "WHERE s.indexInfo = :indexInfo " +
        "AND s.jobType = :jobType " +
        "AND s.result = :result")
    Optional<LocalDate> findLatestTargetDate(
        @Param("indexInfo") IndexInfo indexInfo,
        @Param("jobType") JobType jobType,
        @Param("result") Result result
    );
}