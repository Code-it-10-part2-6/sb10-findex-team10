package com.sb10findexteam6.common.exception;

import java.time.Instant;

public record ErrorResponse(
    Instant timestamp,
    int status,
    String message,
    String details
) {}
