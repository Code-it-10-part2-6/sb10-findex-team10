package com.sb10findexteam6.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

  INVALID_REQUEST(400, "잘못된 요청입니다."),
  INTERNAL_SERVER_ERROR(500, "서버 오류가 발생했습니다."),
  OPEN_API_COMMUNICATION_ERROR(500, "외부 API 통신 중 오류가 발생했습니다.");

  private final int status;
  private final String message;

}
