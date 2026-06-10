package com.woorifisa.won_invest_core_server.domain.account.service;

import com.woorifisa.won_invest_core_server.domain.account.dto.response.InvestAutoInvestExecutionHistoryResponse;
import com.woorifisa.won_invest_core_server.domain.account.exception.InvestAutoInvestExecutionHistoryErrorCode;
import com.woorifisa.won_invest_core_server.domain.account.model.InvestAccount;
import com.woorifisa.won_invest_core_server.domain.account.service.projection.AutoInvestExecutionHistoryView;
import com.woorifisa.won_invest_core_server.domain.autoinvest.model.enums.AutoInvestExecutionStatus;
import com.woorifisa.won_invest_core_server.domain.autoinvest.repository.AutoInvestSweepLedgerRepository;
import com.woorifisa.won_invest_core_server.global.exception.handler.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InvestAutoInvestExecutionHistoryQueryService {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    private static final ZoneId KST_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final InvestAccountQuerySupport investAccountQuerySupport;
    private final AutoInvestSweepLedgerRepository autoInvestSweepLedgerRepository;

    public InvestAutoInvestExecutionHistoryResponse getAutoInvestExecutionHistories(
            UUID accountUuid,
            UUID userUuid,
            OffsetDateTime from,
            OffsetDateTime to,
            String status,
            String ticker,
            String cursor,
            Integer size
    ) {
        validateDateRange(from, to);

        InvestAccount account = investAccountQuerySupport.getAccessibleActiveAccount(
                accountUuid,
                userUuid,
                InvestAutoInvestExecutionHistoryErrorCode.INVALID_ACCOUNT_STATUS
        );

        CursorInfo cursorInfo = parseCursor(cursor);
        AutoInvestExecutionStatus statusFilter = parseStatus(status);
        String normalizedTicker = normalizeTicker(ticker);
        int normalizedSize = normalizeSize(size);

        List<AutoInvestExecutionHistoryView> fetched = autoInvestSweepLedgerRepository.findExecutionHistories(
                account.getInvestAccountUuid(),
                account.getUserUuid(),
                toLocalDateTime(from),
                toLocalDateTime(to),
                statusFilter,
                normalizedTicker,
                cursorInfo.sortDateTime(),
                cursorInfo.sweepExecutionId(),
                normalizedSize + 1
        );

        boolean hasNext = fetched.size() > normalizedSize;
        List<AutoInvestExecutionHistoryView> page = hasNext ? fetched.subList(0, normalizedSize) : fetched;
        String nextCursor = hasNext && !page.isEmpty() ? toCursor(page.get(page.size() - 1)) : null;

        return new InvestAutoInvestExecutionHistoryResponse(
                OffsetDateTime.now(KST_ZONE_ID),
                page.stream().map(this::toHistory).toList(),
                nextCursor,
                hasNext
        );
    }

    private void validateDateRange(OffsetDateTime from, OffsetDateTime to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BusinessException(InvestAutoInvestExecutionHistoryErrorCode.INVALID_QUERY_CONDITION);
        }
    }

    private AutoInvestExecutionStatus parseStatus(String status) {
        String normalized = trimToNull(status);
        if (normalized == null) {
            return null;
        }

        try {
            AutoInvestExecutionStatus parsed = AutoInvestExecutionStatus.valueOf(normalized.toUpperCase(Locale.ROOT));
            if (parsed == AutoInvestExecutionStatus.REQUESTED) {
                throw new BusinessException(InvestAutoInvestExecutionHistoryErrorCode.INVALID_QUERY_CONDITION);
            }
            return parsed;
        } catch (IllegalArgumentException exception) {
            throw new BusinessException(InvestAutoInvestExecutionHistoryErrorCode.INVALID_QUERY_CONDITION);
        }
    }

    private String normalizeTicker(String ticker) {
        String normalized = trimToNull(ticker);
        return normalized == null ? null : normalized.toUpperCase(Locale.ROOT);
    }

    private int normalizeSize(Integer size) {
        if (size == null) {
            return DEFAULT_SIZE;
        }
        if (size <= 0) {
            throw new BusinessException(InvestAutoInvestExecutionHistoryErrorCode.INVALID_QUERY_CONDITION);
        }
        return Math.min(size, MAX_SIZE);
    }

    private CursorInfo parseCursor(String cursor) {
        String normalized = trimToNull(cursor);
        if (normalized == null) {
            return CursorInfo.empty();
        }

        String[] parts = normalized.split("\\|", -1);
        if (parts.length != 2) {
            throw new BusinessException(InvestAutoInvestExecutionHistoryErrorCode.INVALID_QUERY_CONDITION);
        }

        try {
            return new CursorInfo(
                    toLocalDateTime(OffsetDateTime.parse(parts[0])),
                    Long.parseLong(parts[1])
            );
        } catch (RuntimeException exception) {
            throw new BusinessException(InvestAutoInvestExecutionHistoryErrorCode.INVALID_QUERY_CONDITION);
        }
    }

    private String toCursor(AutoInvestExecutionHistoryView view) {
        return view.sortDateTime().atZone(KST_ZONE_ID).toOffsetDateTime() + "|" + view.sweepExecutionId();
    }

    private InvestAutoInvestExecutionHistoryResponse.History toHistory(AutoInvestExecutionHistoryView view) {
        return new InvestAutoInvestExecutionHistoryResponse.History(
                view.sweepExecutionId(),
                view.orderId(),
                view.etfId(),
                view.etfName(),
                view.ticker(),
                view.executionStatus().name(),
                BigDecimal.valueOf(view.requestedKrwAmount()).setScale(2),
                view.orderedQuantity(),
                view.executedQuantity(),
                view.averageExecutionPrice(),
                view.executedAmount(),
                toOffsetDateTime(view.requestedAt()),
                toOffsetDateTime(view.executedAt()),
                view.failureCode(),
                view.failureMessage()
        );
    }

    private LocalDateTime toLocalDateTime(OffsetDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZoneSameInstant(KST_ZONE_ID).toLocalDateTime();
    }

    private OffsetDateTime toOffsetDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(KST_ZONE_ID).toOffsetDateTime();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private record CursorInfo(LocalDateTime sortDateTime, Long sweepExecutionId) {
        private static CursorInfo empty() {
            return new CursorInfo(null, null);
        }
    }
}
