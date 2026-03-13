package com.sb10findexteam6.service;

import com.sb10findexteam6.common.enums.SourceType;
import com.sb10findexteam6.dto.PagingResponse;
import com.sb10findexteam6.dto.indexdata.IndexDataCreateRequest;
import com.sb10findexteam6.dto.indexdata.IndexDataDto;
import com.sb10findexteam6.dto.indexdata.IndexDataSearchCondition;
import com.sb10findexteam6.dto.indexdata.IndexDataUpdateRequest;
import com.sb10findexteam6.entity.IndexData;
import com.sb10findexteam6.entity.IndexInfo;
import com.sb10findexteam6.repository.IndexDataRepository;
import com.sb10findexteam6.repository.IndexInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional
public class IndexDataServiceImpl implements IndexDataService {
    private final IndexDataRepository indexDataRepository;
    private final IndexInfoRepository indexInfoRepository;

  @Override
  public IndexDataDto create(IndexDataCreateRequest request) {
    IndexInfo indexInfo =
        indexInfoRepository.findById(request.indexInfoId())
            .orElseThrow(() -> new IllegalArgumentException("해당 지수 정보가 없습니다."));

    boolean exists =
        indexDataRepository.existsByIndexInfoIdAndBaseDate(
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

    IndexData saved = indexDataRepository.save(indexData);
    return toDto(saved);
    }

    @Override
    public IndexDataDto update(Long id, IndexDataUpdateRequest request) {
        IndexData indexData = indexDataRepository.findById(id)
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
        IndexData indexData = indexDataRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 지수 데이터가 존재하지 않습니다. id=" + id));

        indexDataRepository.delete(indexData);
    }

    @Transactional(readOnly = true)
    @Override
    public IndexDataDto getById(Long id) {
        IndexData indexData = indexDataRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 지수 데이터가 존재하지 않습니다. id=" + id));

        return toDto(indexData);
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<IndexDataDto> getAll(IndexDataSearchCondition condition) {
        // 목록 조회 구현 전 임시 반환
        int size = condition.getSize() != null ? condition.getSize() : 10;
        return new PagingResponse<>(
                List.of(),
                null,
                null,
                size,
                0L,
                false);
    }


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
