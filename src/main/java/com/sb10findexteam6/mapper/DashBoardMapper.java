package com.sb10findexteam6.mapper;

import com.sb10findexteam6.dto.dashboard.ChartDataPoint;
import com.sb10findexteam6.dto.dashboard.IndexChartDto;
import com.sb10findexteam6.dto.dashboard.IndexPerformanceDto;
import com.sb10findexteam6.dto.dashboard.PeriodType;
import com.sb10findexteam6.entity.IndexData;
import com.sb10findexteam6.entity.IndexInfo;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DashBoardMapper {

  @Mapping(target = "indexInfoId", source = "indexInfo.id")
  @Mapping(target = "currentPrice", source = "indexData.closingPrice")
  IndexPerformanceDto toIndexPerformanceDto(IndexInfo indexInfo, IndexData indexData, BigDecimal beforePrice);

  default IndexChartDto toIndexChartDto(
      IndexInfo indexInfo,
      PeriodType periodType,
      List<IndexData> indexData
  ) {
    List<ChartDataPoint> dataPoints = toClosingPricePoints(indexData);
    List<ChartDataPoint> ma5DataPoints = toMovingAveragePoints(indexData, 5);
    List<ChartDataPoint> ma20DataPoints = toMovingAveragePoints(indexData, 20);

    return new IndexChartDto(
        indexInfo.getId(),
        indexInfo.getIndexClassification(),
        indexInfo.getIndexName(),
        periodType,
        dataPoints,
        ma5DataPoints,
        ma20DataPoints
    );
  }

  default List<ChartDataPoint> toClosingPricePoints(List<IndexData> data) {
    return data.stream()
        .map(indexData -> new ChartDataPoint(indexData.getBaseDate(), indexData.getClosingPrice()))
        .toList();
  }

  default List<ChartDataPoint> toMovingAveragePoints(List<IndexData> data, int windowSize) {
    List<ChartDataPoint> result = new ArrayList<>();

    // 이동 평균 구하기 위해 windowSize 개수 만큼의 요소 필요하므로 widowSize보다 작은 값은 스킵
    for (int i = 0; i < data.size(); i++) {
      if (i + 1 < windowSize) {
        continue;
      }

      BigDecimal sum = BigDecimal.ZERO;
      // 현재 인덱스 이전의 window 개수의 요소 합계 계산
      for (int j = i - windowSize + 1; j <= i; j++) {
        BigDecimal price = data.get(j).getClosingPrice();
        if (price == null) {
          sum = null;
          break;
        }
        sum = sum.add(price);
      }

      if (sum == null) {
        continue;
      }

      // 평균 계산
      BigDecimal average = sum.divide(
          BigDecimal.valueOf(windowSize),
          2, // 소수점 둘째 자리까지
          RoundingMode.HALF_UP // 반올림
      );

      result.add(new ChartDataPoint(
          data.get(i).getBaseDate(),
          average
      ));
    }

    return result;
  }
}
