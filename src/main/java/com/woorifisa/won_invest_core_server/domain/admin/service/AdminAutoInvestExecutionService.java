package com.woorifisa.won_invest_core_server.domain.admin.service;

import com.woorifisa.won_invest_core_server.domain.admin.dto.response.AdminAutoInvestExecutionItemResponse;
import com.woorifisa.won_invest_core_server.domain.admin.dto.response.AdminAutoInvestExecutionListResponse;
import com.woorifisa.won_invest_core_server.domain.admin.dto.response.AdminAutoInvestExecutionSummaryResponse;
import com.woorifisa.won_invest_core_server.domain.autoinvest.model.enums.AutoInvestExecutionStatus;
import com.woorifisa.won_invest_core_server.domain.autoinvest.repository.AutoInvestSweepLedgerRepository;
import com.woorifisa.won_invest_core_server.domain.autoinvest.service.projection.AdminAutoInvestExecutionView;
import com.woorifisa.won_invest_core_server.global.exception.code.CommonErrorCode;
import com.woorifisa.won_invest_core_server.global.exception.handler.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminAutoInvestExecutionService {

    private static final int MAX_PAGE_SIZE = 100;

    private final AutoInvestSweepLedgerRepository autoInvestSweepLedgerRepository;

    public AdminAutoInvestExecutionListResponse getExecutions(
            String status,
            String userUuid,
            Long executionId,
            Long sweepRequestId,
            String ticker,
            int page,
            int size
    ) {
        AutoInvestExecutionStatus executionStatus = mapStatus(status);
        UUID parsedUserUuid = parseUuid(userUuid);
        String normalizedTicker = normalizeTicker(ticker);
        Pageable pageable = PageRequest.of(normalizePage(page), normalizeSize(size));

        Page<AdminAutoInvestExecutionView> executions = autoInvestSweepLedgerRepository.findAdminExecutions(
                executionStatus,
                parsedUserUuid,
                executionId,
                sweepRequestId,
                normalizedTicker,
                pageable
        );

        return new AdminAutoInvestExecutionListResponse(
                getSummary(parsedUserUuid, executionId, sweepRequestId, normalizedTicker),
                executions.getContent()
                        .stream()
                        .map(AdminAutoInvestExecutionItemResponse::from)
                        .toList(),
                executions.getNumber(),
                executions.getSize(),
                executions.getTotalElements(),
                executions.getTotalPages()
        );
    }

    private AdminAutoInvestExecutionSummaryResponse getSummary(
            UUID userUuid,
            Long executionId,
            Long sweepRequestId,
            String ticker
    ) {
        long totalCount = autoInvestSweepLedgerRepository.countAdminExecutions(
                null,
                userUuid,
                executionId,
                sweepRequestId,
                ticker
        );
        long exchangeCompletedCount = autoInvestSweepLedgerRepository.countAdminExchangeCompletedExecutions(
                userUuid,
                executionId,
                sweepRequestId,
                ticker
        );
        long orderFailedCount = autoInvestSweepLedgerRepository.countAdminOrderFailedExecutions(
                userUuid,
                executionId,
                sweepRequestId,
                ticker
        );
        long completedCount = autoInvestSweepLedgerRepository.countAdminExecutions(
                AutoInvestExecutionStatus.COMPLETED,
                userUuid,
                executionId,
                sweepRequestId,
                ticker
        );

        return new AdminAutoInvestExecutionSummaryResponse(
                totalCount,
                exchangeCompletedCount,
                orderFailedCount,
                completedCount
        );
    }

    private AutoInvestExecutionStatus mapStatus(String status) {
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
            return null;
        }

        String normalizedStatus = status.toUpperCase(Locale.ROOT);
        return switch (normalizedStatus) {
            case "REQUESTED", "READY", "FX_REQUESTED", "FX_COMPLETED", "ORDER_REQUESTED" ->
                    AutoInvestExecutionStatus.REQUESTED;
            case "COMPLETED" -> AutoInvestExecutionStatus.COMPLETED;
            case "FAILED" -> AutoInvestExecutionStatus.FAILED;
            default -> throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        };
    }

    private UUID parseUuid(String userUuid) {
        if (userUuid == null || userUuid.isBlank()) {
            return null;
        }

        try {
            return UUID.fromString(userUuid);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT_VALUE);
        }
    }

    private String normalizeTicker(String ticker) {
        if (ticker == null || ticker.isBlank()) {
            return null;
        }
        return ticker.trim();
    }

    private int normalizePage(int page) {
        return Math.max(page, 0);
    }

    private int normalizeSize(int size) {
        if (size <= 0) {
            return 20;
        }

        return Math.min(size, MAX_PAGE_SIZE);
    }
}
