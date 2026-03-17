package com.sb10findexteam6.repository;

import com.sb10findexteam6.entity.IndexData;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;

public interface IndexDataRepository extends JpaRepository<IndexData, Long>, IndexDataRepositoryCustom{
    // 같은 (indexInfo/Data) 가진 데이터의 존재여부 확인
    boolean existsByIndexInfoIdAndBaseDate(Long indexInfoId, LocalDate baseDate);

    Optional<IndexData> findByIndexInfoIdAndBaseDate(Long indexInfoId, LocalDate baseDate);


    // 조건이 변하지 않는 정적 쿼리문이므로 Impl 클래스에 넣는 것이 아닌 기본 repository에 구현
    @Query("""
        SELECT d FROM IndexData d
        JOIN FETCH d.indexInfo i
        WHERE i.favorite = true
          AND d.baseDate = (
              SELECT MAX(d2.baseDate)
              FROM IndexData d2
              WHERE d2.indexInfo.id = i.id
          )
        ORDER BY i.indexName ASC
    """)
    List<IndexData> findLatestIndexDataForFavorites();
}
