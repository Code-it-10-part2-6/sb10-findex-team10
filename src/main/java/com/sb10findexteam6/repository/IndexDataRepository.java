package com.sb10findexteam6.repository;

import com.sb10findexteam6.entity.IndexData;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface IndexDataRepository extends JpaRepository<IndexData, Long>, IndexDataRepositoryCustom{
    // 같은 (indexInfo/Data) 가진 데이터의 존재여부 확인
    boolean existsByIndexInfoIdAndBaseDate(Long indexInfoId, LocalDate baseDate);

    Optional<IndexData> findByIndexInfoIdAndBaseDate(Long indexInfoId, LocalDate baseDate);
}
