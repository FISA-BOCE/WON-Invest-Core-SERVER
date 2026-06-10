package com.woorifisa.won_invest_core_server.domain.autoinvest.service.projection;

import com.woorifisa.won_invest_core_server.domain.autoinvest.model.enums.AutoInvestExecutionStatus;
import com.woorifisa.won_invest_core_server.domain.autoinvest.model.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record AdminAutoInvestExecutionView(
        Long executionId,
        Long sweepRequestId,
        UUID userUuid,
        Long etfId,
        String ticker,
        AutoInvestExecutionStatus status,
        String failureCode,
        String failureMessage,
        Long orderId,
        OrderStatus orderStatus,
        Long executionLedgerId,
        BigDecimal fxRateSnapshot,
        LocalDateTime requestedAt,
        LocalDateTime completedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
