package com.woorifisa.won_invest_core_server.domain.autoinvest.dto.response;

import com.woorifisa.won_invest_core_server.domain.autoinvest.exception.enums.AutoInvestFailureCode;
import com.woorifisa.won_invest_core_server.domain.autoinvest.model.enums.AutoInvestExecutionStatus;

public record AutoInvestExecutionResponse(
        Long sweepExecutionId,
        String idempotencyKey,
        AutoInvestExecutionStatus status,
        String failureCode,
        String failureMessage
) {
    public static AutoInvestExecutionResponse completed(Long executionId, String idempotencyKey) {
        return new AutoInvestExecutionResponse(
                executionId,
                idempotencyKey,
                AutoInvestExecutionStatus.COMPLETED,
                null,
                null
        );
    }

    public static AutoInvestExecutionResponse failed(String idempotencyKey, AutoInvestFailureCode failureCode) {
        return new AutoInvestExecutionResponse(
                null,
                idempotencyKey,
                AutoInvestExecutionStatus.FAILED,
                failureCode.getCode(),
                failureCode.getMessage()
        );
    }
}
