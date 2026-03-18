package com.sb10findexteam6.service;

import com.sb10findexteam6.common.enums.SourceType;
import com.sb10findexteam6.common.exception.BusinessException;
import com.sb10findexteam6.common.exception.ErrorCode;
import com.sb10findexteam6.dto.CursorPageResponse;
import com.sb10findexteam6.dto.indexdata.*;
import com.sb10findexteam6.entity.IndexData;
import com.sb10findexteam6.entity.IndexInfo;
import com.sb10findexteam6.mapper.PagingMapper;
import com.sb10findexteam6.repository.IndexDataRepository;
import com.sb10findexteam6.repository.IndexInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class IndexDataServiceImpl implements IndexDataService {

    private final IndexDataRepository indexDataRepository;
    private final IndexInfoRepository indexInfoRepository;
    private final PagingMapper pagingMapper;

    @Override
    public IndexDataDto create(IndexDataCreateRequest request) {
        IndexInfo indexInfo =
                indexInfoRepository
                        .findById(request.indexInfoId())
                        .orElseThrow(
                                () -> new BusinessException(
                                        ErrorCode.INVALID_REQUEST,
                                        "해당 지수 정보가 없습니다. indexInfoId= " + request.indexInfoId()));

        boolean exists =
                indexDataRepository.existsByIndexInfoIdAndBaseDate(
                        request.indexInfoId(), request.baseDate());

        if (exists) {
            throw new BusinessException(
                    ErrorCode.INVALID_REQUEST,
                    "이미 해당 지수 정보와 날짜로 등록된 지수 데이터가 존재합니다. indexInfoId= "
                            + request.indexInfoId()
                            + ", baseDate= "
                            + request.baseDate());
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
        IndexData indexData =
                indexDataRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new BusinessException(
                                        ErrorCode.INVALID_REQUEST,
                                        "해당 지수 데이터가 존재하지 않습니다. id= " + id));

        indexData.update(
                request.marketPrice(),
                request.closingPrice(),
                request.highPrice(),
                request.lowPrice(),
                request.versus(),
                request.fluctuationRate(),
                request.tradingQuantity(),
                request.tradingPrice(),
                request.marketTotalAmount());

        return toDto(indexData);
    }

    @Override
    public void delete(Long id) {
        IndexData indexData =
                indexDataRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new BusinessException(
                                        ErrorCode.INVALID_REQUEST,
                                        "해당 지수 데이터가 존재하지 않습니다. id= " + id));

        indexDataRepository.delete(indexData);
    }

    @Override
    @Transactional(readOnly = true)
    public IndexDataDto getById(Long id) {
        IndexData indexData =
                indexDataRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new BusinessException(
                                        ErrorCode.INVALID_REQUEST,
                                        "해당 지수 데이터가 존재하지 않습니다. id= " + id));

        return toDto(indexData);
    }

    @Override
    @Transactional(readOnly = true)
    public CursorPageResponse<IndexDataDto> getAll(IndexDataSearchCondition condition) {
        int size = condition.getSize() != null ? condition.getSize() : 10;

        Long resolvedIdAfter = pagingMapper.resolveIdAfter(condition.getCursor(), condition.getIdAfter());
        condition.setIdAfter(resolvedIdAfter);

        List<IndexDataDto> results = indexDataRepository.search(condition).stream()
                .map(this::toDto)
                .toList();

        long totalElements = indexDataRepository.count(condition);

        return pagingMapper.toResponse(
                results,
                size,
                totalElements,
                IndexDataDto::id
        );
    }
    @Override
    @Transactional(readOnly = true)
    public byte[] export(IndexDataSearchCondition condition) {
        List<IndexDataDto> results = indexDataRepository.searchForExport(condition).stream()
                .map(this::toDto)
                .toList();

        StringBuilder sb = new StringBuilder();
        sb.append("id,indexInfoId,baseDate,sourceType,marketPrice,closingPrice,highPrice,lowPrice,versus,fluctuationRate,tradingQuantity,tradingPrice,marketTotalAmount\n");

        for (IndexDataDto dto : results) {
            sb.append(dto.id()).append(",")
                    .append(dto.indexInfoId()).append(",")
                    .append(dto.baseDate()).append(",")
                    .append(dto.sourceType()).append(",")
                    .append(dto.marketPrice()).append(",")
                    .append(dto.closingPrice()).append(",")
                    .append(dto.highPrice()).append(",")
                    .append(dto.lowPrice()).append(",")
                    .append(dto.versus()).append(",")
                    .append(dto.fluctuationRate()).append(",")
                    .append(dto.tradingQuantity()).append(",")
                    .append(dto.tradingPrice()).append(",")
                    .append(dto.marketTotalAmount())
                    .append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
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
