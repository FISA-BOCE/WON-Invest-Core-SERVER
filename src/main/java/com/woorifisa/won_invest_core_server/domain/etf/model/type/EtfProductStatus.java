package com.woorifisa.won_invest_core_server.domain.etf.model.type;

public enum EtfProductStatus {

    ACTIVE,    // 정상 제공 가능 상품
    INACTIVE,  // 비활성 상품, 서비스 제공 제외
    SUSPENDED, // 거래 일시 중지 또는 점검 상태
    DELISTED   // 상장폐지 또는 제공 종료 상품
}