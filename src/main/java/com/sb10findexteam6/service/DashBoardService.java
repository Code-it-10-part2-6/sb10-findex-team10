package com.sb10findexteam6.service;

import com.sb10findexteam6.common.exception.BusinessException;
import com.sb10findexteam6.common.exception.ErrorCode;
import com.sb10findexteam6.dto.dashboard.IndexChartDto;
import com.sb10findexteam6.dto.dashboard.IndexPerformanceDto;
import com.sb10findexteam6.dto.dashboard.PeriodType;
import com.sb10findexteam6.entity.IndexData;
import com.sb10findexteam6.entity.IndexInfo;
import com.sb10findexteam6.mapper.DashBoardMapper;
import com.sb10findexteam6.repository.IndexInfoRepository;
import com.sb10findexteam6.repository.IndexDataRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
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

    private final IndexInfoRepository indexInfoRepository;
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

  public IndexChartDto getChartIndex(Long id, PeriodType periodType) {
    IndexInfo indexInfo = indexInfoRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "해당 지수 정보가 없습니다. id=" + id));

    if (periodType == PeriodType.DAILY
        || periodType == PeriodType.WEEKLY) {
      throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "지원하지 않는 기간 타입입니다: " + periodType);
    }

    LocalDate today = LocalDate.now();
    LocalDate startDate = periodType.getStartDate(today);

    List<IndexData> indexDatas = indexDataRepository
        .findByIndexInfoIdAndBaseDateBetweenOrderByBaseDateAsc(id, startDate, today);

    return dashBoardMapper.toIndexChartDto(indexInfo, periodType, indexDatas);
  }
}
