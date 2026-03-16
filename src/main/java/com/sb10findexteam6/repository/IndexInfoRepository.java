package com.sb10findexteam6.repository;

import com.sb10findexteam6.common.enums.SourceType;
import com.sb10findexteam6.entity.IndexInfo;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IndexInfoRepository extends JpaRepository<IndexInfo, Long> {

  boolean existsByIndexClassificationAndIndexName(
      String indexClassification,
      String indexName
  ); // 지수 정보 등록의 {지수 분류명}, {지수명} 조합값은 중복되면 안됩니다.

  // ASC (id > idAfter)
  @Query("""
        SELECT i FROM IndexInfo i
        WHERE (:indexClassification IS NULL OR i.indexClassification LIKE %:indexClassification%)
          AND (:indexName IS NULL OR i.indexName LIKE %:indexName%)
          AND (:favorite IS NULL OR i.favorite = :favorite)
          AND (:idAfter IS NULL OR i.id > :idAfter)
    """)
  List<IndexInfo> findByConditionsAsc(
      @Param("indexClassification") String indexClassification,
      @Param("indexName") String indexName,
      @Param("favorite") Boolean favorite,
      @Param("idAfter") Long idAfter,
      Pageable pageable
  );

  // DESC (id < idAfter)
  @Query("""
        SELECT i FROM IndexInfo i
        WHERE (:indexClassification IS NULL OR i.indexClassification LIKE %:indexClassification%)
          AND (:indexName IS NULL OR i.indexName LIKE %:indexName%)
          AND (:favorite IS NULL OR i.favorite = :favorite)
          AND (:idAfter IS NULL OR i.id < :idAfter)
    """)
  List<IndexInfo> findByConditionsDesc(
      @Param("indexClassification") String indexClassification,
      @Param("indexName") String indexName,
      @Param("favorite") Boolean favorite,
      @Param("idAfter") Long idAfter,
      Pageable pageable
  );

  // totalElements용 count
  @Query("""
        SELECT COUNT(i) FROM IndexInfo i
        WHERE (:indexClassification IS NULL OR i.indexClassification LIKE %:indexClassification%)
          AND (:indexName IS NULL OR i.indexName LIKE %:indexName%)
          AND (:favorite IS NULL OR i.favorite = :favorite)
    """)
  long countByConditions(
      @Param("indexClassification") String indexClassification,
      @Param("indexName") String indexName,
      @Param("favorite") Boolean favorite
  );

  // 특정 출처(sourceType)를 가 지수 목록 조회
  // 스케줄러가 OpenAPI 연동 지수 찾을 때 사용
  @Query("SELECT a.indexInfo FROM AutoSyncConfig a " +
      "WHERE a.enabled = true AND a.indexInfo.sourceType = :sourceType")
  List<IndexInfo> findEnabledAutoSyncIndexes(@Param("sourceType") SourceType sourceType);
}