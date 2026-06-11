package com.woorifisa.won_invest_core_server.domain.account.exception;

import com.woorifisa.won_invest_core_server.global.exception.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InvestAutoInvestExecutionHistoryErrorCode implements ErrorCode {
    INVALID_ACCOUNT_STATUS(HttpStatus.BAD_REQUEST, "INVEST_400_001", "정상 상태의 증권계좌가 아닙니다."),
    INVALID_QUERY_CONDITION(HttpStatus.BAD_REQUEST, "INVEST_400_002", "자동 투자 체결 이력 조회 조건이 올바르지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
