package com.woorifisa.won_invest_core_server.domain.autoinvest.exception.code;

import com.woorifisa.won_invest_core_server.global.exception.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AutoInvestFailureCode implements ErrorCode {
    INVALID_EVENT_TYPE(HttpStatus.OK, "SWEEP_FAIL_001", "스윕 요청 이벤트가 아닙니다."),
    INVEST_ACCOUNT_NOT_FOUND(HttpStatus.OK, "SWEEP_FAIL_002", "증권 계좌가 없습니다."),
    INVEST_ACCOUNT_NOT_ACTIVE(HttpStatus.OK, "SWEEP_FAIL_003", "활성 증권 계좌가 아닙니다."),
    ETF_NOT_FOUND(HttpStatus.OK, "SWEEP_FAIL_004", "ETF 상품을 찾을 수 없습니다."),
    ETF_NOT_AVAILABLE(HttpStatus.OK, "SWEEP_FAIL_005", "자동투자 가능한 ETF가 아닙니다."),
    PRICE_UNAVAILABLE(HttpStatus.OK, "SWEEP_FAIL_006", "ETF 현재가를 조회할 수 없습니다."),
    FX_RATE_UNAVAILABLE(HttpStatus.OK, "SWEEP_FAIL_007", "환율을 조회할 수 없습니다."),
    INSUFFICIENT_AMOUNT(HttpStatus.OK, "SWEEP_FAIL_008", "매수 가능한 금액이 부족합니다."),
    EXECUTION_LEDGER_NOT_FOUND(HttpStatus.OK, "SWEEP_FAIL_009", "기존 스윕 주문의 체결 원장을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
