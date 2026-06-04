package com.woorifisa.won_invest_core_server.domain.account.service.projection;

import com.woorifisa.won_invest_core_server.domain.account.model.InvestOrderType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecentExecutionView(
        LocalDateTime executedAt,
        String ticker,
        BigDecimal executionQuantity,
        InvestOrderType orderType
) {
}
