package com.woorifisa.won_invest_core_server.domain.autoinvest.dto.response;

import com.woorifisa.won_invest_core_server.domain.autoinvest.model.AutoInvestSweepLedger;
import com.woorifisa.won_invest_core_server.domain.autoinvest.model.enums.AutoInvestExecutionStatus;

public record AutoInvestExecutionResponse(
        Long sweepExecutionId,
        String idempotencyKey,
        AutoInvestExecutionStatus status,
        String failureCode,
        String failureMessage
) {
    public static AutoInvestExecutionResponse from(AutoInvestSweepLedger ledger) {
        return new AutoInvestExecutionResponse(
                ledger.getSweepExecutionId(),
                ledger.getIdempotencyKey(),
                ledger.getStatus(),
                ledger.getFailureCode(),
                ledger.getFailureMessage()
        );
    }
}
