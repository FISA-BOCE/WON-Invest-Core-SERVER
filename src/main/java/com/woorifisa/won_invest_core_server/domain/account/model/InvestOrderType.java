package com.woorifisa.won_invest_core_server.domain.account.model;

import java.util.EnumSet;
import java.util.Set;

public enum InvestOrderType {
    BUY,
    SELL,
    PURCHASE,
    AUTO_BUY;

    public static Set<InvestOrderType> buyTypes() {
        return EnumSet.of(BUY, PURCHASE, AUTO_BUY);
    }
}
