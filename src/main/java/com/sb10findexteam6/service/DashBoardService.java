package com.sb10findexteam6.service;

import com.sb10findexteam6.dto.dashboard.IndexPerformanceDto;
import com.sb10findexteam6.entity.IndexData;
import com.sb10findexteam6.entity.IndexInfo;
import com.sb10findexteam6.mapper.DashBoardMapper;
import com.sb10findexteam6.repository.IndexDataRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashBoardService {

    private final IndexDataRepository indexDataRepository;
    private final DashBoardMapper dashBoardMapper;

    /**
     * 대시보드 - 주요 지수 현황 요약 (즐겨찾기 지수)
     */
    public List<IndexPerformanceDto> getFavoriteIndexesPerformance() {
        // 즐겨찾기 된 지수의 최신 IndexData 목록을 가져옴
        List<IndexData> latestDataList = indexDataRepository.findLatestIndexDataForFavorites();

        return latestDataList.stream().map(indexData -> {
                IndexInfo indexInfo = indexData.getIndexInfo();

                BigDecimal beforePrice = indexData.getClosingPrice().subtract(indexData.getVersus());

                return dashBoardMapper.toIndexPerformanceDto(indexInfo, indexData, beforePrice);
            })
            .toList();
    }
}