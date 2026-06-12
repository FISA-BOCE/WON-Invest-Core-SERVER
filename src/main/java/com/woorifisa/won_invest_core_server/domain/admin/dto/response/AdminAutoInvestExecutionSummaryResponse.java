package com.woorifisa.won_invest_core_server.domain.admin.dto.response;

public record AdminAutoInvestExecutionSummaryResponse(
        long totalCount,
        long exchangeCompletedCount,
        long orderFailedCount,
        long completedCount
) {
}
