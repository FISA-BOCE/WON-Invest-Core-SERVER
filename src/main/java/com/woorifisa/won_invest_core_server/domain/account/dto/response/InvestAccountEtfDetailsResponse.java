package com.woorifisa.won_invest_core_server.domain.account.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record InvestAccountEtfDetailsResponse(
        BigDecimal totalEvaluationAmount,
        BigDecimal profitLossAmount,
        BigDecimal profitLossRate,
        List<HoldingResponse> holdings,
        List<RecentExecutionResponse> recentExecutions
) {

    public record HoldingResponse(
            Long etfId,
            String etfName,
            String ticker,
            BigDecimal holdingQuantity,
            BigDecimal averageBuyPrice,
            BigDecimal evaluationAmount,
            BigDecimal profitLossAmount,
            BigDecimal profitLossRate
    ) {
    }

    public record RecentExecutionResponse(
            LocalDateTime executedAt,
            String ticker,
            BigDecimal executionQuantity,
            String executionType
    ) {
    }
}
