package com.sb10findexteam6.service;

import com.sb10findexteam6.common.enums.SourceType;
import com.sb10findexteam6.dto.indexdata.IndexDataCreateRequest;
import com.sb10findexteam6.dto.indexdata.IndexDataDto;
import com.sb10findexteam6.dto.indexdata.IndexDataUpdateRequest;
import com.sb10findexteam6.entity.IndexData;
import com.sb10findexteam6.repository.JPAIndexDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class IndexDataServiceImpl implements IndexDataService {
    private final JPAIndexDataRepository jpaIndexDataRepository;
    private final JPAIndexInfoRepository jpaIndexInfoRepository;

  @Override
  public IndexDataDto create(IndexDataCreateRequest request) {
    IndexInfo indexInfo =
        jpaIndexInfoRepository.findById(request.indexInfoId())
            .orElseThrow(() -> new IllegalArgumentException("해당 지수 정보가 없습니다."));

    boolean exists =
        jpaIndexDataRepository.existsByIndexInfoIdAndBaseDate(
            request.indexInfoId(), request.baseDate());
    if (exists) {
      throw new IllegalArgumentException("이미 해당 지수와 날짜로 등록된 지수 데이터가 존재합니다.");
    }

    IndexData indexData =
        new IndexData(
            indexInfo,
            request.baseDate(),
            SourceType.USER,
            request.marketPrice(),
            request.closingPrice(),
            request.highPrice(),
            request.lowPrice(),
            request.versus(),
            request.fluctuationRate(),
            request.tradingQuantity(),
            request.tradingPrice(),
            request.marketTotalAmount());

    IndexData saved = jpaIndexDataRepository.save(indexData);
    return toDto(saved);
    }

    @Override
    public IndexDataDto update(Long id, IndexDataUpdateRequest request) {
        IndexData indexData = jpaIndexDataRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("수정할 지수 데이터가 존재하지 않습니다. id=" + id));

        indexData.update(
                request.marketPrice(),
                request.closingPrice(),
                request.highPrice(),
                request.lowPrice(),
                request.versus(),
                request.fluctuationRate(),
                request.tradingQuantity(),
                request.tradingPrice(),
                request.marketTotalAmount()
        );

        return toDto(indexData);
    }

    @Override
    public void delete(Long id) {
        IndexData indexData = jpaIndexDataRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 지수 데이터가 존재하지 않습니다. id=" + id));

        jpaIndexDataRepository.delete(indexData);
    }

    @Transactional(readOnly = true)
    @Override
    public IndexDataDto getById(Long id) {
        IndexData indexData = jpaIndexDataRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 지수 데이터가 존재하지 않습니다. id=" + id));

        return toDto(indexData);
    }
    /*
    @Override
    public CursorPageResponse<IndexDataDto> getAll(IndexDataSearchCondition condition) {
        // 커스텀 레포 연결 전 임시 구현
        return new CursorPageResponse<>(
                List.of(),
                null,
                null,
                condition.size() != null ? condition.size() : 10,
                0L,
                false
        );
    }
    */

    private IndexDataDto toDto(IndexData indexData) {
        return new IndexDataDto(
                indexData.getId(),
                indexData.getIndexInfo().getId(),
                indexData.getBaseDate(),
                indexData.getSourceType().name(),
                indexData.getMarketPrice(),
                indexData.getClosingPrice(),
                indexData.getHighPrice(),
                indexData.getLowPrice(),
                indexData.getVersus(),
                indexData.getFluctuationRate(),
                indexData.getTradingQuantity(),
                indexData.getTradingPrice(),
                indexData.getMarketTotalAmount()
        );
    }
}
