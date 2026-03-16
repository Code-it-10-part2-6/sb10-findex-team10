package com.sb10findexteam6.service;


import com.sb10findexteam6.common.enums.SourceType;
import com.sb10findexteam6.dto.CursorPageResponse;
import com.sb10findexteam6.dto.CursorPageResponseIndexInfoDto;
import com.sb10findexteam6.dto.indexinfo.IndexInfoCreateRequest;
import com.sb10findexteam6.dto.indexinfo.IndexInfoDto;
import com.sb10findexteam6.dto.indexinfo.IndexInfoSearchRequest;
import com.sb10findexteam6.dto.indexinfo.IndexInfoSummaryDto;
import com.sb10findexteam6.dto.indexinfo.IndexInfoUpdateRequest;
import com.sb10findexteam6.dto.openapi.FscIndexResponseDto;
import com.sb10findexteam6.entity.AutoSyncConfig;
import com.sb10findexteam6.entity.IndexInfo;
import com.sb10findexteam6.mapper.IndexInfoMapper;
import com.sb10findexteam6.mapper.PagingMapper;
import com.sb10findexteam6.repository.AutoSyncConfigRepository;
import com.sb10findexteam6.repository.IndexInfoRepository;
import com.sb10findexteam6.service.openapi.OpenApiFetchService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class IndexInfoServiceImpl implements IndexInfoService{
  private final IndexInfoRepository indexInfoRepository;
  private final AutoSyncConfigRepository autoSyncConfigRepository;
  private final IndexInfoMapper indexInfoMapper;
  private final PagingMapper pagingMapper;
  private final OpenApiFetchService openApiFetchService;


  @Override
  @Transactional
  public IndexInfoDto create(IndexInfoCreateRequest request) {
    if(indexInfoRepository.existsByIndexClassificationAndIndexName(
        request.indexClassification(),
        request.indexName()))
    {
      throw new IllegalArgumentException("이미 존재하는 지수 정보입니다.");
    }

    IndexInfo indexInfo = new IndexInfo(
        request.indexClassification(),
        request.indexName(),
        request.employedItemsCount(),
        request.basePointInTime(),
        request.baseIndex(),
        SourceType.USER,
        request.favorite()
    );
    indexInfoRepository.save(indexInfo);

    AutoSyncConfig autoSyncConfig = new AutoSyncConfig(indexInfo);
    autoSyncConfigRepository.save(autoSyncConfig);

    return indexInfoMapper.toDto(indexInfo);
  }

  @Override
  @Transactional
  public List<IndexInfoDto> createFromOpenApi(String targetDate) {
    FscIndexResponseDto response = openApiFetchService.fetchStockMarketIndex(targetDate, 100, 1);

    List<IndexInfoDto> result = new ArrayList<>();
    for (FscIndexResponseDto.Item item : response.response().body().items().item()) {
      if (indexInfoRepository.existsByIndexClassificationAndIndexName(
          item.idxCsf(), item.idxNm())) {
        continue;
      }

      IndexInfo indexInfo = new IndexInfo(
          item.idxCsf(),
          item.idxNm(),
          Integer.parseInt(item.epyItmsCnt()),
          LocalDate.parse(item.basPntm(), DateTimeFormatter.ofPattern("yyyyMMdd")),
          new BigDecimal(item.basIdx()),
          SourceType.OPEN_API,
          false
      );
      indexInfoRepository.save(indexInfo);
      autoSyncConfigRepository.save(new AutoSyncConfig(indexInfo));
      result.add(indexInfoMapper.toDto(indexInfo));
    }
    return result;
  }


  @Override
  @Transactional
  public IndexInfoDto update(Long id, IndexInfoUpdateRequest request) {
    IndexInfo indexInfo = indexInfoRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("해당 id의 지수 정보가 없습니다"));

    indexInfo.update(
        request.employedItemsCount(),
        request.basePointInTime(),
        request.baseIndex(),
        request.favorite()
    );

    return indexInfoMapper.toDto(indexInfo);
  }

  @Override
  @Transactional(readOnly = true)
  public IndexInfoDto findById(Long id) {
    IndexInfo indexInfo = indexInfoRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("해당 id의 지수 정보가 없습니다"));
    return indexInfoMapper.toDto(indexInfo);
  }

  @Override
  @Transactional
  public void delete(Long id) {
    IndexInfo indexInfo = indexInfoRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("해당 id의 지수 정보가 없습니다"));
    autoSyncConfigRepository.deleteByIndexInfo(indexInfo);//일단 하이버네이트라서 적용 안될 수 있으니 후에 수정가능성 있음
    indexInfoRepository.delete(indexInfo);
  }

  @Override
  @Transactional(readOnly = true)
  public CursorPageResponseIndexInfoDto findIndexInfoList(IndexInfoSearchRequest request) {
    // 1. 정렬 방향 판별
    boolean isAsc = "asc".equalsIgnoreCase(request.sortDirection());
    String field = request.sortField() == null ? "indexClassification" : request.sortField();

    // 2. 정렬 조건 생성
    Sort sort = Sort.by(isAsc ? Sort.Direction.ASC : Sort.Direction.DESC, field)
        .and(Sort.by(isAsc ? Sort.Direction.ASC : Sort.Direction.DESC, "id"));

    // 3. size+1개 조회
    Pageable pageable = PageRequest.of(0, request.size() + 1, sort);

    // 4. cursor -> idAfter 변환
    Long resolvedIdAfter = pagingMapper.resolveIdAfter(request.cursor(), request.idAfter());

    // 5. ASC/DESC에 따라 다른 쿼리 호출
    List<IndexInfo> results = isAsc
        ? indexInfoRepository.findByConditionsAsc(
        request.indexClassification(),
        request.indexName(),
        request.favorite(),
        resolvedIdAfter,
        pageable)
        : indexInfoRepository.findByConditionsDesc(
            request.indexClassification(),
            request.indexName(),
            request.favorite(),
            resolvedIdAfter,
            pageable);

    // 6. totalElements
    long totalElements = indexInfoRepository.countByConditions(
        request.indexClassification(),
        request.indexName(),
        request.favorite()
    );

    // 7. 페이징 응답 생성
    CursorPageResponse<IndexInfoDto> page = pagingMapper.toResponse(
        results.stream().map(indexInfoMapper::toDto).toList(),
        request.size(),
        totalElements,
        IndexInfoDto::id
    );

    return new CursorPageResponseIndexInfoDto(
        page.content(),
        page.nextCursor(),
        page.nextIdAfter(),
        page.size(),
        page.totalElements(),
        page.hasNext()
    );
  }

  @Override
  @Transactional(readOnly = true)
  public List<IndexInfoSummaryDto> findSummaryList() {
    return indexInfoRepository.findAll().stream()
        .map(indexInfoMapper::toSummaryDto)
        .toList();
  }
}
