package com.sb10findexteam6.dto.dashboard;

public record RankedIndexPerformanceDto(
    IndexPerformanceDto performance,
    int rank
) {

}
