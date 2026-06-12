package com.woorifisa.won_invest_core_server.domain.admin.dto.response;

import com.woorifisa.won_invest_core_server.domain.autoinvest.model.enums.AutoInvestExecutionStatus;
import com.woorifisa.won_invest_core_server.domain.autoinvest.model.enums.OrderStatus;
import com.woorifisa.won_invest_core_server.domain.autoinvest.service.projection.AdminAutoInvestExecutionView;

import java.time.LocalDateTime;

public record AdminAutoInvestExecutionItemResponse(
        Long executionId,
        Long sweepRequestId,
        String userUuid,
        Long etfId,
        String ticker,
        String executionStatus,
        String fxStatus,
        String orderStatus,
        String failReason,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime updatedAt
) {

    private static final String FX_RATE_UNAVAILABLE = "SWEEP_FAIL_007";

    public static AdminAutoInvestExecutionItemResponse from(AdminAutoInvestExecutionView view) {
        return new AdminAutoInvestExecutionItemResponse(
                view.executionId(),
                view.sweepRequestId(),
                view.userUuid().toString(),
                view.etfId(),
                view.ticker(),
                mapExecutionStatus(view),
                mapFxStatus(view),
                mapOrderStatus(view),
                view.failureMessage(),
                view.requestedAt(),
                view.completedAt(),
                view.updatedAt()
        );
    }

    private static String mapExecutionStatus(AdminAutoInvestExecutionView view) {
        if (view.status() == AutoInvestExecutionStatus.COMPLETED) {
            return "COMPLETED";
        }
        if (view.status() == AutoInvestExecutionStatus.FAILED) {
            return "FAILED";
        }
        if (view.orderId() != null) {
            return "ORDER_REQUESTED";
        }
        return "READY";
    }

    private static String mapFxStatus(AdminAutoInvestExecutionView view) {
        if (view.fxRateSnapshot() != null) {
            return "COMPLETED";
        }
        if (view.status() == AutoInvestExecutionStatus.FAILED
                && FX_RATE_UNAVAILABLE.equals(view.failureCode())) {
            return "FAILED";
        }
        return "REQUESTED";
    }

    private static String mapOrderStatus(AdminAutoInvestExecutionView view) {
        if (view.orderStatus() == OrderStatus.COMPLETED) {
            return "FILLED";
        }
        if (view.orderStatus() == OrderStatus.FAILED) {
            return "FAILED";
        }
        if (view.orderStatus() == OrderStatus.REQUESTED) {
            return "REQUESTED";
        }
        if (view.status() == AutoInvestExecutionStatus.COMPLETED) {
            return "CANCELLED";
        }
        return "REQUESTED";
    }
}
