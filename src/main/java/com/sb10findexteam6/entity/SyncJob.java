package com.sb10findexteam6.entity;

import com.sb10findexteam6.common.entity.BaseEntity;
import com.sb10findexteam6.common.enums.JobType;
import com.sb10findexteam6.common.enums.Result;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "sync_job")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SyncJob extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "index_info_id", nullable = false)
    private IndexInfo indexInfo;
    @Column(name = "job_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private JobType jobType;
    @Column(name = "target_date")
    private LocalDate targetDate;
    @Column(name = "worker", nullable = false)
    private String worker;
    @Column(name = "job_time", nullable = false)
    private LocalDateTime jobTime;
    @Column(name = "result", nullable = false)
    @Enumerated(EnumType.STRING)
    private Result result;

    public SyncJob(
            IndexInfo indexInfo,
            JobType jobType,
            LocalDate targetDate,
            String worker,
            LocalDateTime jobTime,
            Result result
    ) {
        this.indexInfo = indexInfo;
        this.jobType = jobType;
        this.targetDate = targetDate;
        this.worker = worker;
        this.jobTime = jobTime;
        this.result = result;
    }
}
