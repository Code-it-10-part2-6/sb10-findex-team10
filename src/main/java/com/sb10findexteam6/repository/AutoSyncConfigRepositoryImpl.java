package com.sb10findexteam6.repository;

import com.sb10findexteam6.dto.autosyncconfig.AutoSyncConfigSearchCondition;
import com.sb10findexteam6.entity.AutoSyncConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
public class AutoSyncConfigRepositoryImpl implements AutoSyncConfigRepositoryCustom {

  @PersistenceContext
  private EntityManager em;

  @Override
  public AutoSyncConfigSearchResult search(AutoSyncConfigSearchCondition condition) {
    // 조건 추가 전의 기본 쿼리
    StringBuilder fromWhere = new StringBuilder("""
        from AutoSyncConfig ascfg
        join fetch ascfg.indexInfo idx
        where 1=1
        """);

    // 개수를 위한 별도 쿼리
    StringBuilder countFromWhere = new StringBuilder("""
        from AutoSyncConfig ascfg
        join ascfg.indexInfo idx
        where 1=1
        """);

    Map<String, Object> params = new HashMap<>();

    if (condition.getIndexInfoId() != null) {
      fromWhere.append(" and idx.id = :indexInfoId ");
      countFromWhere.append(" and idx.id = :indexInfoId ");
      params.put("indexInfoId", condition.getIndexInfoId());
    }

    if (condition.getEnabled() != null) {
      fromWhere.append(" and ascfg.enabled = :enabled ");
      countFromWhere.append(" and ascfg.enabled = :enabled ");
      params.put("enabled", condition.getEnabled());
    }

    if (condition.getIdAfter() != null) {
      fromWhere.append(" and ascfg.id > :idAfter ");
      countFromWhere.append(" and ascfg.id > :idAfter ");
      params.put("idAfter", condition.getIdAfter());
    }

    String orderBy = buildOrderBy(condition);

    TypedQuery<AutoSyncConfig> query = em.createQuery(
        "select ascfg " + fromWhere + orderBy,
        AutoSyncConfig.class
    );

    TypedQuery<Long> countQuery = em.createQuery(
        "select count(ascfg) " + countFromWhere,
        Long.class
    );

    params.forEach((k, v) -> {
      query.setParameter(k, v);
      countQuery.setParameter(k, v);
    });

    int size = condition.getSize() != null ? condition.getSize() : 10;

    // hasNext 판단 위해 1개 더 조회
    query.setMaxResults(size + 1);

    List<AutoSyncConfig> content = query.getResultList();
    long totalCount = countQuery.getSingleResult();

    return new AutoSyncConfigSearchResult(content, totalCount);
  }

  private String buildOrderBy(AutoSyncConfigSearchCondition condition) {
    String sortField = StringUtils.hasText(condition.getSortField())
        ? condition.getSortField()
        : "indexName";

    String sortDirection = "desc".equalsIgnoreCase(condition.getSortDirection())
        ? "desc"
        : "asc";

    List<String> orders = new ArrayList<>();

    switch (sortField) {
      case "enabled" -> orders.add("ascfg.enabled " + sortDirection);
      case "indexName" -> orders.add("idx.indexName " + sortDirection);
      default -> orders.add("idx.indexName asc");
    }

    // 커서가 id 기반이라 보조 정렬로 id를 꼭 붙여주는 게 안전함
    orders.add("ascfg.id asc");

    return " order by " + String.join(", ", orders);
  }
}