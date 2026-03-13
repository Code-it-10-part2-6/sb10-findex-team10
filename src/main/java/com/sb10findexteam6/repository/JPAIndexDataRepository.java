package com.sb10findexteam6.repository;

import com.sb10findexteam6.entity.IndexData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface JPAIndexDataRepository extends JpaRepository<IndexData, Long> {
    boolean existsByIndexInfoIdAndBaseDate(Long indexInfoId, LocalDate baseDate);
}
