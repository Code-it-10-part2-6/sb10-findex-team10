package com.sb10findexteam6.repository;

// 동적 조회를 위한 커스텀 레포 구현체
import com.sb10findexteam6.common.exception.BusinessException;
import com.sb10findexteam6.common.exception.ErrorCode;
import com.sb10findexteam6.dto.indexdata.IndexDataSearchCondition;
import com.sb10findexteam6.dto.indexdata.IndexDataSortField;
import com.sb10findexteam6.entity.IndexData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class IndexDataRepositoryImpl implements IndexDataRepositoryCustom {

  @PersistenceContext private EntityManager em;

  @Override
  public List<IndexData> search(IndexDataSearchCondition condition) {
    // jpql 기본문 시작 -> where 1=1 은 뒤에 동적 추가 편하게 하기 위해서
    StringBuilder jpql = new StringBuilder("select d from IndexData d where 1=1");
    Map<String, Object> params = new HashMap<>();
    // 검색 조건이 존재하면 and 문으로 추가해서 직접 쿼리에 추가하는 방식.
    if (condition.getIndexInfoId() != null) {
      jpql.append(" and d.indexInfo.id = :indexInfoId");
      params.put("indexInfoId", condition.getIndexInfoId());
    }

    if (condition.getStartDate() != null) {
      jpql.append(" and d.baseDate >= :startDate");
      params.put("startDate", condition.getStartDate());
    }

    if (condition.getEndDate() != null) {
      jpql.append(" and d.baseDate <= :endDate");
      params.put("endDate", condition.getEndDate());
    }

    // sortField (정렬조건)
    IndexDataSortField sortField = resolveSortField(condition.getSortField());
    String sortDirection = resolveSortDirection(condition.getSortDirection());

    // baseDate 기준 정렬일 때만 커서 페이지네이션 지원
    if(condition.getCursor() != null && condition.getIdAfter() != null){
      if(sortField != IndexDataSortField.BASE_DATE){
        throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "현재 커서 페이지네이션은 baseDate 정렬만 지원합니다."
        );
      }

      LocalDate cursorDate;
      try {
        cursorDate = LocalDate.parse(condition.getCursor());
      } catch (Exception e) {
        throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "cursor 값이 날짜 형식이 아닙니다: " + condition.getCursor()
        );
      }

      if ("desc".equals(sortDirection)) {
        jpql.append(" and (d.baseDate < :cursorDate or (d.baseDate = :cursorDate and d.id < : idAfter))");
      } else {
        jpql.append(" and (d.baseDate > :cursorDate or (d.baseDate = :cursorDate and d.id > :idAfter))");
      }

      params.put("cursorDate", cursorDate);
      params.put("idAfter", condition.getIdAfter());
    }

    // 같은 baseDate 값이 있을 떄 id로 구분
    jpql.append(" order by d.")
            .append(sortField.getValue())
            .append(" ")
            .append(sortDirection)
            .append(", d.id ")
            .append(sortDirection);

    TypedQuery<IndexData> query = em.createQuery(jpql.toString(), IndexData.class);
    params.forEach(query::setParameter);

    int size = condition.getSize() != null ? condition.getSize() : 10;
    query.setMaxResults(size + 1); // hasNext 때문에

    return query.getResultList();
  }

  @Override
  public long count(IndexDataSearchCondition condition) {
    StringBuilder jpql = new StringBuilder("select count(d) from IndexData d where 1=1");
    Map<String, Object> params = new HashMap<>();

    appendWhereClause(jpql, params, condition);

    TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
    params.forEach(query::setParameter);

    return query.getSingleResult();
  }

  // 클래스 내 사용 메서드들
  //
  private void appendWhereClause(
          StringBuilder jpql,
          Map<String, Object> params,
          IndexDataSearchCondition condition
  ) {
    if (condition.getIndexInfoId() != null) {
      jpql.append(" and d.indexInfo.id = :indexInfoId");
      params.put("indexInfoId", condition.getIndexInfoId());
    }

    if (condition.getStartDate() != null) {
      jpql.append(" and d.baseDate >= :startDate");
      params.put("startDate", condition.getStartDate());
    }

    if (condition.getEndDate() != null) {
      jpql.append(" and d.baseDate <= :endDate");
      params.put("endDate", condition.getEndDate());
    }
  }

  // 정렬 조건 기본값은 baseDate / desc 순으로
  private IndexDataSortField resolveSortField(String sortField) {
    if (sortField == null || sortField.isBlank()) {
      return IndexDataSortField.BASE_DATE;
    }
    return IndexDataSortField.from(sortField);
  }

  private String resolveSortDirection(String sortDirection) {
    if (sortDirection == null || sortDirection.isBlank()) {
      return "desc";
    }

    String direction = sortDirection.toLowerCase();
    if (!direction.equals("asc") && !direction.equals("desc")) {
      throw new BusinessException(ErrorCode.INVALID_REQUEST, "유효하지 않은 정렬 방향입니다: " + direction);
    }
    return direction;
  }
}
