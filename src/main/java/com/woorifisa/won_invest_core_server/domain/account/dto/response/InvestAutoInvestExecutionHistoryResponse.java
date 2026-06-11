package com.woorifisa.won_invest_core_server.domain.account.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record InvestAutoInvestExecutionHistoryResponse(
        OffsetDateTime baseDateTime,
        List<History> histories,
        String nextCursor,
        boolean hasNext
) {
    public record History(
            Long sweepExecutionId,
            Long orderId,
            Long etfId,
            String etfName,
            String ticker,
            String executionStatus,
            BigDecimal requestedKrwAmount,
            BigDecimal orderedQuantity,
            BigDecimal executedQuantity,
            BigDecimal averageExecutionPrice,
            BigDecimal executedAmount,
            OffsetDateTime requestedAt,
            OffsetDateTime executedAt,
            String failureCode,
            String failureMessage
    ) {
    }
}
