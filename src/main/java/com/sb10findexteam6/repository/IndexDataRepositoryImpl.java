package com.sb10findexteam6.repository;
// 동적 조회를 위한 커스텀 구현체 필요
import com.sb10findexteam6.common.exception.BusinessException;
import com.sb10findexteam6.common.exception.ErrorCode;
import com.sb10findexteam6.dto.indexdata.IndexDataSearchCondition;
import com.sb10findexteam6.dto.indexdata.IndexDataSortField;
import com.sb10findexteam6.entity.IndexData;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class IndexDataRepositoryImpl implements IndexDataRepositoryCustom {

  @PersistenceContext
  private EntityManager em;

  // 목록 조건 조회
  @Override
  public List<IndexData> search(IndexDataSearchCondition condition) {
    StringBuilder jpql = new StringBuilder("select d from IndexData d where 1=1");
    Map<String, Object> params = new HashMap<>(); //실제 값은 -> params.put("indexInfoId", 3L) 이런식으로 저장

    appendWhereClause(jpql, params, condition); // where 조건절 붙이는

    IndexDataSortField sortField = resolveSortField(condition.getSortField()); //sortField enum으로 변경
    String sortDirection = resolveSortDirection(condition.getSortDirection());
    String sortFieldPath = "d." + sortField.getEntityField(); // 정렬에 사용할 필드 경로 만드는.. d.baseDate 처럼

    // 커서 페이지네이션 조건 추가
    if (condition.getIdAfter() != null) {
      // 단순히 id값만 아니아 현재 정렬 기준값도 알아야 예를 들어 id=100인 데이터의 closingPrice 가 얼마인지
      //알아야 그 다음 데이터부터 이어서 조회할 수 있기 때문
      IndexData cursorEntity = em.find(IndexData.class, condition.getIdAfter());

      if (cursorEntity == null) {
        throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "존재하지 않는 페이지네이션 기준 id입니다. idAfter= " + condition.getIdAfter()
        );
      }
      // 정렬 기준값 추출
      Object cursorValue = extractSortValue(cursorEntity, sortField);
      // 내림차 순일때 다음페이지는 현재 커서값보다 더 작은 값 of 같다면 id가 더 작은 값으로 정렬
      if ("desc".equals(sortDirection)) {
        jpql.append(" and (")
                .append(sortFieldPath)
                .append(" < :cursorValue or (")
                .append(sortFieldPath)
                .append(" = :cursorValue and d.id < :idAfter))");
      } else {
        jpql.append(" and (")
                .append(sortFieldPath)
                .append(" > :cursorValue or (")
                .append(sortFieldPath)
                .append(" = :cursorValue and d.id > :idAfter))");
      }

      params.put("cursorValue", cursorValue);
      params.put("idAfter", condition.getIdAfter());
    }

    jpql.append(" order by ")
            .append(sortFieldPath)
            .append(" ")
            .append(sortDirection)
            // id도 정렬 기준에 추가하는 이유는 값이 같은 데이터들도 처리해줘야하기 떄문
            .append(", d.id ")
            .append(sortDirection);

    // 쿼리 객체 생성 및 파라미터 바인딩 -> query.setParameter("indexInfoId", 1L);
    TypedQuery<IndexData> query = em.createQuery(jpql.toString(), IndexData.class);
    params.forEach(query::setParameter);
    // 조회 개수 설정 그리고 다음 페이지가 있는지 확인위해 1개만 더 조회.
    int size = condition.getSize() != null ? condition.getSize() : 10;
    query.setMaxResults(size + 1);

    return query.getResultList();
  }

  // CSV Export
  @Override
  public List<IndexData> searchForExport(IndexDataSearchCondition condition) {
    StringBuilder jpql = new StringBuilder("select d from IndexData d where 1=1");
    Map<String, Object> params = new HashMap<>();

    appendWhereClause(jpql, params, condition);

    IndexDataSortField sortField = resolveSortField(condition.getSortField());
    String sortDirection = resolveSortDirection(condition.getSortDirection());
    String sortFieldPath = "d." + sortField.getEntityField();

    jpql.append(" order by ")
            .append(sortFieldPath)
            .append(" ")
            .append(sortDirection)
            .append(", d.id ")
            .append(sortDirection);

    TypedQuery<IndexData> query = em.createQuery(jpql.toString(), IndexData.class);
    params.forEach(query::setParameter);

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
// 정렬기준 default 값은 sortField로 고정
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
      throw new BusinessException(
              ErrorCode.INVALID_REQUEST,
              "유효하지 않은 정렬 방향입니다: " + direction
      );
    }
    return direction;
  }

  private Object extractSortValue(IndexData indexData, IndexDataSortField sortField) {
    return switch (sortField) {
      case BASE_DATE -> indexData.getBaseDate();
      case MARKET_PRICE -> indexData.getMarketPrice();
      case CLOSING_PRICE -> indexData.getClosingPrice();
      case HIGH_PRICE -> indexData.getHighPrice();
      case LOW_PRICE -> indexData.getLowPrice();
      case VERSUS -> indexData.getVersus();
      case FLUCTUATION_RATE -> indexData.getFluctuationRate();
      case TRADING_QUANTITY -> indexData.getTradingQuantity();
      case TRADING_PRICE -> indexData.getTradingPrice();
      case MARKET_TOTAL_AMOUNT -> indexData.getMarketTotalAmount();
    };
  }
}
