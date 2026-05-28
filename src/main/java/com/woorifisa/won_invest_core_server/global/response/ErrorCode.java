package com.woorifisa.won_invest_core_server.global.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    AUTH_401_001(HttpStatus.UNAUTHORIZED, "AUTH_401_001", "인증이 필요합니다."),
    AUTH_403_001(HttpStatus.FORBIDDEN, "AUTH_403_001", "해당 요청에 대한 권한이 없습니다."),
    COM_500_001(HttpStatus.INTERNAL_SERVER_ERROR, "COM_500_001", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String msg;
}