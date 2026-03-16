package com.sb10findexteam6.service.openapi;

import com.sb10findexteam6.common.enums.SourceType;
import com.sb10findexteam6.dto.openapi.FscIndexResponseDto;
import com.sb10findexteam6.dto.openapi.OpenApiIndexDataSyncResultDto;
import com.sb10findexteam6.entity.IndexData;
import com.sb10findexteam6.entity.IndexInfo;
import com.sb10findexteam6.repository.IndexDataRepository;
import com.sb10findexteam6.repository.IndexInfoRepository;
import com.sb10findexteam6.util.OpenApiParseUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class IndexDataOpenApiSyncServiceImpl implements IndexDataOpenApiSyncService {

    private final OpenApiFetchService openApiFetchService;
    private final IndexInfoRepository indexInfoRepository;
    private final IndexDataRepository indexDataRepository;

    @Override
    public OpenApiIndexDataSyncResultDto sync(String targetDate, int numOfRows, int pageNo) {
        FscIndexResponseDto response =
                openApiFetchService.fetchStockMarketIndex(targetDate, numOfRows, pageNo);

        List<FscIndexResponseDto.Item> items = response.response().body().items().item();

        int skippedCount = 0;
        int createdCount = 0;
        int updatedCount = 0;

        for (FscIndexResponseDto.Item item : items) {
            IndexInfo indexInfo = indexInfoRepository
                    .findByIndexClassificationAndIndexName(item.idxCsf(), item.idxNm())
                    .orElse(null);

            if (indexInfo == null) {
                skippedCount++;
                continue;
            }

            LocalDate baseDate = OpenApiParseUtils.parseDate(item.basDt());

            IndexData indexData = indexDataRepository
                    .findByIndexInfoIdAndBaseDate(indexInfo.getId(), baseDate)
                    .orElse(null);

            if (indexData == null) {
                IndexData newIndexData = new IndexData(
                        indexInfo,
                        baseDate,
                        SourceType.OPEN_API,
                        OpenApiParseUtils.parseBigDecimal(item.mkp()),
                        OpenApiParseUtils.parseBigDecimal(item.clpr()),
                        OpenApiParseUtils.parseBigDecimal(item.hipr()),
                        OpenApiParseUtils.parseBigDecimal(item.lopr()),
                        OpenApiParseUtils.parseBigDecimal(item.vs()),
                        OpenApiParseUtils.parseBigDecimal(item.fltRt()),
                        OpenApiParseUtils.parseLong(item.trqu()),
                        OpenApiParseUtils.parseLong(item.trPrc()),
                        OpenApiParseUtils.parseLong(item.lstgMrktTotAmt())
                );
                indexDataRepository.save(newIndexData);
                createdCount++;
            } else {
                indexData.update(
                        OpenApiParseUtils.parseBigDecimal(item.mkp()),
                        OpenApiParseUtils.parseBigDecimal(item.clpr()),
                        OpenApiParseUtils.parseBigDecimal(item.hipr()),
                        OpenApiParseUtils.parseBigDecimal(item.lopr()),
                        OpenApiParseUtils.parseBigDecimal(item.vs()),
                        OpenApiParseUtils.parseBigDecimal(item.fltRt()),
                        OpenApiParseUtils.parseLong(item.trqu()),
                        OpenApiParseUtils.parseLong(item.trPrc()),
                        OpenApiParseUtils.parseLong(item.lstgMrktTotAmt())
                );
                updatedCount++;
            }
        }

        return new OpenApiIndexDataSyncResultDto(
                items.size(),
                skippedCount,
                createdCount,
                updatedCount
        );
    }
}
