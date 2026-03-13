package com.sb10findexteam6.repository;

import com.sb10findexteam6.entity.IndexData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface JPAIndexDataRepository extends JpaRepository<IndexData, Long> {
    // 같은 (indexInfo/Data) 가진 데이터의 존재여부 확인
    boolean existsByIndexInfoIdAndBaseDate(Long indexInfoId, LocalDate baseDate);
}
