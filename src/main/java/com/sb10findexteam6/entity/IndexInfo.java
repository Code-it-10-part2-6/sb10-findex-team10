package com.sb10findexteam6.entity;

import com.sb10findexteam6.common.entity.BaseEntity;
import com.sb10findexteam6.common.enums.SourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "index_info")
@NoArgsConstructor
@Getter
public class IndexInfo extends BaseEntity {

  @Column(nullable = false)
  private String indexClassification;

  @Column(nullable = false)
  private String indexName;

  private int employedItemsCount;

  private LocalDate basePointInTime;

  private BigDecimal baseIndex;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private SourceType sourceType;

  @Column(nullable = false)
  private boolean favorite;

  public IndexInfo(String indexClassification, String indexName,
      int employedItemsCount, LocalDate basePointInTime,
      BigDecimal baseIndex, SourceType sourceType, boolean favorite) {
    this.indexClassification = indexClassification;
    this.indexName = indexName;
    this.employedItemsCount = employedItemsCount;
    this.basePointInTime = basePointInTime;
    this.baseIndex = baseIndex;
    this.sourceType = sourceType;
    this.favorite = favorite;
  }

  public void update(Integer employedItemsCount, LocalDate basePointInTime,
      BigDecimal baseIndex, boolean favorite) {
    this.employedItemsCount = employedItemsCount;
    this.basePointInTime = basePointInTime;
    this.baseIndex = baseIndex;
    this.favorite = favorite;
  }

}
