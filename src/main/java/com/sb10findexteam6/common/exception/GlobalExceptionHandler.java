package com.sb10findexteam6.common.exception;

import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {



  // 비즈니스 예외
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {

    ErrorCode errorCode = e.getErrorCode();

    ErrorResponse response = new ErrorResponse(
        Instant.now(),
        errorCode.getStatus(),
        errorCode.getMessage(),
        e.getDetails()
    );

    return ResponseEntity
        .status(errorCode.getStatus())
        .body(response);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(
      MethodArgumentTypeMismatchException e
  ) {

    String parameter = e.getName();
    String value = String.valueOf(e.getValue());

    String details = parameter + " 값 '" + value + "' 이(가) 올바른 형식이 아닙니다.";

    ErrorResponse response = new ErrorResponse(
        Instant.now(),
        HttpStatus.BAD_REQUEST.value(),
        "잘못된 요청입니다.",
        details
    );

    return ResponseEntity.badRequest().body(response);
  }
}
