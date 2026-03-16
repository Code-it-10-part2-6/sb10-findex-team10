package com.sb10findexteam6.repository;

import com.sb10findexteam6.common.enums.SourceType;
import com.sb10findexteam6.entity.IndexInfo;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndexInfoRepository extends JpaRepository<IndexInfo, Long> {

    // 특정 출처(sourceType)를 가 지수 목록 조회
    // 스케줄러가 OpenAPI 연동 지수 찾을 때 사용
    List<IndexInfo> findAllBySourceType(SourceType sourceType);

}
