package com.woorifisa.won_invest_core_server.domain.account.exception;

import com.woorifisa.won_invest_core_server.global.exception.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InvestAccountErrorCode implements ErrorCode {
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "INVEST_400_001", "입력값 형식이 올바르지 않습니다."),
    ACCOUNT_ALREADY_CONNECTED(HttpStatus.BAD_REQUEST, "INVEST_400_003", "이미 연결된 증권계좌가 존재합니다."),
    REQUIRED_TERMS_NOT_AGREED(HttpStatus.BAD_REQUEST, "INVEST_400_004", "필수 약관에 동의하지 않았습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
