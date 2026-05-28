package com.woorifisa.won_invest_core_server.global.response;

import com.woorifisa.won_invest_core_server.global.exception.code.ErrorCode;
import org.springframework.http.ResponseEntity;

public record ErrorResponse(
        int status,
        String code,
        String message
) {

    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getHttpStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage()
        );
    }

    public static ResponseEntity<ErrorResponse> of(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.from(errorCode));
    }
}

/* 형식 :
{
  "status": 401,
  "code": "AUTH_401_001",
  "message": "인증이 필요합니다."
}
 */