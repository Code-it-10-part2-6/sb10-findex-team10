package com.sb10findexteam6.dto.openapi;
// 우선 연동이력 붙이기 전에 임시로 사용하는 dto
public record OpenApiIndexDataSyncResultDto(
        // OpenApi에서 받아온 item 수
        int totalFetchedCount,
        // 해당 IndexInfo 없어서 건더뛴 수
        int skippedCount,
        // 새로 생성한 IndexData 수
        int createdCount,
        // 수정한 IndexData 수
        int updatedCount
) {}
