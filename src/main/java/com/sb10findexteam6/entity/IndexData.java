package com.sb10findexteam6.entity;

import com.sb10findexteam6.common.entity.BaseEntity;
import com.sb10findexteam6.common.enums.SourceType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Entity
@Table(
        name = "index_data",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_index_data_index_info_base_date",
                        columnNames = {"index_info_id", "base_date"}
                )
        }
)
@NoArgsConstructor
public class IndexData extends BaseEntity {

    //FK
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "index_info_id", nullable = false)
    private IndexInfo indexInfo;

    @Column(name="base_date", nullable = false)
    private LocalDate baseDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SourceType sourceType;
    // 현재가, 시장가
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal marketPrice;
    // 오늘 종가
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal closingPrice;
    // 고가
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal highPrice;
    // 저가
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal lowPrice;

    // 전일대비값
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal versus;
    // 등락률 (오늘-어제) / 어제값 * 100
    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal fluctuationRate;
    //거래 총 수량
    @Column(nullable = false)
    private Long tradingQuantity;
    // 거래 대금
    @Column(nullable = false)
    private Long tradingPrice;
    // 시가 총액
    @Column(nullable = false)
    private Long marketTotalAmount;

    public IndexData(
            IndexInfo indexInfo,
            LocalDate baseDate,
            SourceType sourceType,
            BigDecimal marketPrice,
            BigDecimal closingPrice,
            BigDecimal highPrice,
            BigDecimal lowPrice,
            BigDecimal versus,
            BigDecimal fluctuationRate,
            Long tradingQuantity,
            Long tradingPrice,
            Long marketTotalAmount
    ) {
        this.indexInfo = indexInfo;
        this.baseDate = baseDate;
        this.sourceType = sourceType;
        this.marketPrice = marketPrice;
        this.closingPrice = closingPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.versus = versus;
        this.fluctuationRate = fluctuationRate;
        this.tradingQuantity = tradingQuantity;
        this.tradingPrice = tradingPrice;
        this.marketTotalAmount = marketTotalAmount;
    }

    public void update(
            BigDecimal marketPrice,
            BigDecimal closingPrice,
            BigDecimal highPrice,
            BigDecimal lowPrice,
            BigDecimal versus,
            BigDecimal fluctuationRate,
            Long tradingQuantity,
            Long tradingPrice,
            Long marketTotalAmount
    ) {
        this.marketPrice = marketPrice;
        this.closingPrice = closingPrice;
        this.highPrice = highPrice;
        this.lowPrice = lowPrice;
        this.versus = versus;
        this.fluctuationRate = fluctuationRate;
        this.tradingQuantity = tradingQuantity;
        this.tradingPrice = tradingPrice;
        this.marketTotalAmount = marketTotalAmount;
    }
}


