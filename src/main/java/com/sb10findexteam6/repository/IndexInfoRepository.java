package com.sb10findexteam6.repository;

import com.sb10findexteam6.common.enums.SourceType;
import com.sb10findexteam6.entity.IndexInfo;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IndexInfoRepository extends JpaRepository<IndexInfo, Long> {

  // 임시 추가.
  Optional<IndexInfo> findByIndexClassificationAndIndexName(
          String indexClassification,
          String indexName
  );

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

  @Query("""
    SELECT i
    FROM IndexInfo i
    JOIN AutoSyncConfig a ON a.indexInfo = i
    WHERE a.enabled = true
      AND i.sourceType = :sourceType
""")
  List<IndexInfo> findEnabledAutoSyncIndexes(@Param("sourceType") SourceType sourceType);

}
