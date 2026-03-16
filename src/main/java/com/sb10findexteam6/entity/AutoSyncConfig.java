package com.sb10findexteam6.entity;

import com.sb10findexteam6.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "auto_sync_config")
@Getter
@NoArgsConstructor
public class AutoSyncConfig extends BaseEntity {

  @Column(name = "enabled", nullable = false)
  private boolean enabled;

  @OneToOne(fetch = FetchType.LAZY, optional = false, cascade = CascadeType.ALL)
  @JoinColumn(name = "index_info_id", nullable = false, unique = true)
  private IndexInfo indexInfo;

  public AutoSyncConfig(IndexInfo indexInfo) {
    this.enabled = false;  // 기본값 비활성화
    this.indexInfo = indexInfo;
  }
}
