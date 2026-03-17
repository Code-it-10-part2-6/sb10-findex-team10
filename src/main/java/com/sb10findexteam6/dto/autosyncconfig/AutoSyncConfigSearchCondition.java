package com.sb10findexteam6.dto.autosyncconfig;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AutoSyncConfigSearchCondition {
  private Long indexInfoId;
  private Boolean enabled;
  private Long idAfter;
  private String cursor;
  private String sortField;
  private String sortDirection;
  private Integer size;
}
