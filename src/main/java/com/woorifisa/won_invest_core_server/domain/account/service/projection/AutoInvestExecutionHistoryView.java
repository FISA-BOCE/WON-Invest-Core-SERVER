package com.woorifisa.won_invest_core_server.domain.account.service.projection;

import com.woorifisa.won_invest_core_server.domain.autoinvest.model.enums.AutoInvestExecutionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AutoInvestExecutionHistoryView(
        Long sweepExecutionId,
        Long orderId,
        Long etfId,
        String etfName,
        String ticker,
        AutoInvestExecutionStatus executionStatus,
        Long requestedKrwAmount,
        BigDecimal orderedQuantity,
        BigDecimal executedQuantity,
        BigDecimal averageExecutionPrice,
        BigDecimal executedAmount,
        LocalDateTime requestedAt,
        LocalDateTime executedAt,
        String failureCode,
        String failureMessage,
        LocalDateTime sortDateTime
) {
}
