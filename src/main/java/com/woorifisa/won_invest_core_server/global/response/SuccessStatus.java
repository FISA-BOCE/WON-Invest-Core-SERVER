package com.woorifisa.won_invest_core_server.global.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SuccessStatus {

    OK(HttpStatus.OK, "COM_200_001", "OK"),
    CREATED(HttpStatus.CREATED, "COM_201_001", "CREATED"),
    NO_CONTENT(HttpStatus.NO_CONTENT, "COM_204_001", "NO_CONTENT"),

    // ETF
    ETF_PRODUCT_SYNCED(HttpStatus.OK, "ETF_200_001", "ETF 상품 마스터 동기화가 완료되었습니다."),

    INVEST_ACCOUNT_CREATED(HttpStatus.CREATED, "INVST_201_001", "증권계좌 개설이 완료되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    SuccessStatus(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

}
