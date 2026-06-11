package com.woorifisa.won_invest_core_server.domain.autoinvest.repository;

import com.woorifisa.won_invest_core_server.domain.account.service.projection.AutoInvestExecutionHistoryView;
import com.woorifisa.won_invest_core_server.domain.autoinvest.model.enums.AutoInvestExecutionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AutoInvestSweepLedgerQueryRepository {

    List<AutoInvestExecutionHistoryView> findExecutionHistories(
            UUID accountUuid,
            UUID userUuid,
            LocalDateTime from,
            LocalDateTime to,
            AutoInvestExecutionStatus status,
            String ticker,
            LocalDateTime cursorDateTime,
            Long cursorSweepExecutionId,
            int limit
    );
}
