package com.sb10findexteam6.dto.autosyncconfig;

public record AutoSyncConfigDto(
    Long id,
    Long indexInfoId,
    String indexClassification,
    String indexName,
    Boolean enabled
) {

}
