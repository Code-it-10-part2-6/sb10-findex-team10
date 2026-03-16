package com.sb10findexteam6.mapper;

import com.sb10findexteam6.common.enums.SourceType;
import com.sb10findexteam6.dto.openapi.FscIndexResponseDto;
import com.sb10findexteam6.entity.IndexData;
import com.sb10findexteam6.entity.IndexInfo;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class OpenApiDataMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    // OpenAPI 응답 DTO(Item)를 IndexData 엔티티로 변환합니다.
    public IndexData mapToEntity(FscIndexResponseDto.Item item, IndexInfo indexInfo) {
        return new IndexData(
            indexInfo,
            LocalDate.parse(item.basDt(), DATE_FORMATTER), //
            SourceType.OPEN_API,
            new BigDecimal(item.mkp()),         // 시가 (시장가)
            new BigDecimal(item.clpr()),        // 종가
            new BigDecimal(item.hipr()),        // 고가
            new BigDecimal(item.lopr()),        // 저가
            new BigDecimal(item.vs()),          // 대비
            new BigDecimal(item.fltRt()),       // 등락률
            Long.parseLong(item.trqu()),        // 거래량
            Long.parseLong(item.trPrc()),       // 거래대금
            Long.parseLong(item.lstgMrktTotAmt()) // 시가총액
        );
    }
}