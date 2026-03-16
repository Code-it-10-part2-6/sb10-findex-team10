package com.sb10findexteam6.repository.specification;

import com.sb10findexteam6.entity.IndexInfo;
import com.sb10findexteam6.common.enums.JobType;
import com.sb10findexteam6.common.enums.Result;
import com.sb10findexteam6.entity.SyncJob;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SyncJobSpecification {

  public static Specification<SyncJob> hasJobType(JobType jobType) {
    return (root, query, cb) -> jobType == null ? null : cb.equal(root.get("jobType"), jobType);
  }

  public static Specification<SyncJob> hasIndexInfoId(Long indexInfoId) {
    return (root, query, cb) -> {
      if (indexInfoId == null) return null;
      Join<SyncJob, IndexInfo> indexInfoJoin = root.join("indexInfo");
      return cb.equal(indexInfoJoin.get("id"), indexInfoId);
    };
  }

  public static Specification<SyncJob> targetDateGoe(LocalDate from) {
    return (root, query, cb) ->
        from == null ? null : cb.greaterThanOrEqualTo(root.get("targetDate"), from);
  }

  public static Specification<SyncJob> targetDateLoe(LocalDate to) {
    return (root, query, cb) ->
        to == null ? null : cb.lessThanOrEqualTo(root.get("targetDate"), to);
  }

  public static Specification<SyncJob> workerContains(String worker) {
    return (root, query, cb) ->
        (worker == null || worker.isBlank())
            ? null
            : cb.like(root.get("worker"), "%" + worker + "%");
  }

  public static Specification<SyncJob> hasResult(Result result) {
    return (root, query, cb) -> result == null ? null : cb.equal(root.get("result"), result);
  }

  public static Specification<SyncJob> jobTimeGoe(LocalDateTime from) {
    return (root, query, cb) ->
        from == null ? null : cb.greaterThanOrEqualTo(root.get("jobTime"), from);
  }

  public static Specification<SyncJob> jobTimeLoe(LocalDateTime to) {
    return (root, query, cb) -> to == null ? null : cb.lessThanOrEqualTo(root.get("jobTime"), to);
  }
}
