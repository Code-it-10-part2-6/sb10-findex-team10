package com.sb10findexteam6.service;

import com.sb10findexteam6.common.exception.BusinessException;
import com.sb10findexteam6.common.exception.ErrorCode;
import com.sb10findexteam6.dto.dashboard.IndexChartDto;
import com.sb10findexteam6.dto.dashboard.IndexPerformanceDto;
import com.sb10findexteam6.dto.dashboard.IndexPerformanceRankDto;
import com.sb10findexteam6.dto.dashboard.PeriodType;
import com.sb10findexteam6.entity.IndexData;
import com.sb10findexteam6.entity.IndexInfo;
import com.sb10findexteam6.mapper.DashBoardMapper;
import com.sb10findexteam6.repository.IndexInfoRepository;
import com.sb10findexteam6.repository.IndexDataRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

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
    /**
     * 지수 성과 분석 랭킹
     * - 최신 종가 기준
     * - DAILY: 전일 대비
     * - WEEKLY: 전주 대비
     * - MONTHLY: 전월 대비
     */
    public List<IndexPerformanceRankDto> getPerformanceRank(
            Long indexInfoId,
            PeriodType periodType,
            int limit
    ) {
        if (limit <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "limit는 1 이상이어야 합니다.");
        }

        List<IndexData> latestDataList = indexDataRepository.findLatestIndexData(indexInfoId);

        List<IndexPerformanceDto> performances = latestDataList.stream()
                .map(latestData -> {
                    LocalDate comparisonDate = periodType.getComparisonDate(latestData.getBaseDate());

                    return indexDataRepository
                            .findTopByIndexInfoIdAndBaseDateLessThanEqualOrderByBaseDateDesc(
                                    latestData.getIndexInfo().getId(),
                                    comparisonDate
                            )
                            .map(beforeData -> {
                                BigDecimal currentPrice = latestData.getClosingPrice();
                                BigDecimal beforePrice = beforeData.getClosingPrice();

                                if (currentPrice == null || beforePrice == null) {
                                    return null;
                                }

                                if (beforePrice.compareTo(BigDecimal.ZERO) == 0) {
                                    return null;
                                }

                                BigDecimal versus = currentPrice.subtract(beforePrice);
                                BigDecimal fluctuationRate = versus
                                        .divide(beforePrice, 6, RoundingMode.HALF_UP)
                                        .multiply(BigDecimal.valueOf(100))
                                        .setScale(2, RoundingMode.HALF_UP);

                                IndexInfo indexInfo = latestData.getIndexInfo();

                                return new IndexPerformanceDto(
                                        indexInfo.getId(),
                                        indexInfo.getIndexClassification(),
                                        indexInfo.getIndexName(),
                                        versus,
                                        fluctuationRate,
                                        currentPrice,
                                        beforePrice
                                );
                            })
                            .orElse(null);
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(IndexPerformanceDto::fluctuationRate).reversed())
                .limit(limit)
                .toList();

        return toRankedResponse(performances);
    }

    private List<IndexPerformanceRankDto> toRankedResponse(List<IndexPerformanceDto> performances) {
        return java.util.stream.IntStream.range(0, performances.size())
                .mapToObj(i -> {
                    IndexPerformanceDto dto = performances.get(i);
                    return new IndexPerformanceRankDto(
                            i + 1,
                            dto.indexInfoId(),
                            dto.indexClassification(),
                            dto.indexName(),
                            dto.versus(),
                            dto.fluctuationRate(),
                            dto.currentPrice(),
                            dto.beforePrice()
                    );
                })
                .toList();
    }
}
