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

  @Override
  public List<IndexData> search(IndexDataSearchCondition condition) {
    StringBuilder jpql = new StringBuilder("select d from IndexData d where 1=1");
    Map<String, Object> params = new HashMap<>();

    appendWhereClause(jpql, params, condition);

    IndexDataSortField sortField = resolveSortField(condition.getSortField());
    String sortDirection = resolveSortDirection(condition.getSortDirection());
    String sortFieldPath = "d." + sortField.getEntityField();

    if (condition.getIdAfter() != null) {
      IndexData cursorEntity = em.find(IndexData.class, condition.getIdAfter());

      if (cursorEntity == null) {
        throw new BusinessException(
                ErrorCode.INVALID_REQUEST,
                "존재하지 않는 페이지네이션 기준 id입니다. idAfter= " + condition.getIdAfter()
        );
      }

      Object cursorValue = extractSortValue(cursorEntity, sortField);

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
            .append(", d.id ")
            .append(sortDirection);

    TypedQuery<IndexData> query = em.createQuery(jpql.toString(), IndexData.class);
    params.forEach(query::setParameter);

    int size = condition.getSize() != null ? condition.getSize() : 10;
    query.setMaxResults(size + 1);

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
