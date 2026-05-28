package com.woorifisa.won_invest_core_server.global.response;

public record ErrorResponse(
        int status,
        String code,
        String msg
) {

    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(
                errorCode.getHttpStatus().value(),
                errorCode.getCode(),
                errorCode.getMsg()
        );
    }
}

/* 형식 :
{
  "status": 401,
  "code": "AUTH_401_001",
  "msg": "인증이 필요합니다."
}
 */