package com.woorifisa.won_invest_core_server.global.response;

import com.woorifisa.won_invest_core_server.global.exception.code.ErrorCode;
import org.springframework.http.ResponseEntity;

public record ErrorResponse(
        int status,
        String code,
        String message
) {
    public static ResponseEntity<ErrorResponse> of(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(new ErrorResponse(
                        errorCode.getHttpStatus().value(),
                        errorCode.getCode(),
                        errorCode.getMessage()
                ));
    }
}