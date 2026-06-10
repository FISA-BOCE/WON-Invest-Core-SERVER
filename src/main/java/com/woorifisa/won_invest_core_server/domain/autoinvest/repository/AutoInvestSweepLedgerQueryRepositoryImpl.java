package com.woorifisa.won_invest_core_server.domain.autoinvest.repository;

import com.woorifisa.won_invest_core_server.domain.account.service.projection.AutoInvestExecutionHistoryView;
import com.woorifisa.won_invest_core_server.domain.autoinvest.model.enums.AutoInvestExecutionStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class AutoInvestSweepLedgerQueryRepositoryImpl implements AutoInvestSweepLedgerQueryRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<AutoInvestExecutionHistoryView> findExecutionHistories(
            UUID accountUuid,
            UUID userUuid,
            LocalDateTime from,
            LocalDateTime to,
            AutoInvestExecutionStatus status,
            String ticker,
            LocalDateTime cursorDateTime,
            Long cursorSweepExecutionId,
            int limit
    ) {
        StringBuilder sql = new StringBuilder("""
                select
                    s.sweep_execution_id,
                    o.order_id,
                    s.etf_id,
                    etf.etf_name,
                    upper(etf.ticker) as ticker,
                    s.status,
                    s.krw_amount,
                    coalesce(o.order_quantity, s.order_quantity) as ordered_quantity,
                    e.execution_quantity,
                    e.execution_price,
                    e.execution_amount,
                    s.requested_at,
                    e.executed_at,
                    s.failure_code,
                    s.failure_message,
                    coalesce(e.executed_at, s.requested_at) as sort_date_time
                from auto_invest_sweep_ledger s
                join invest_etf_product etf
                    on etf.etf_id = s.etf_id
                left join invest_order_ledger o
                    on o.order_id = s.order_id
                left join invest_execution_ledger e
                    on e.execution_id = s.execution_ledger_id
                where s.user_uuid = :userUuid
                  and s.status in ('COMPLETED', 'FAILED')
                  and (s.order_id is null or o.invest_account_uuid = :accountUuid)
                """);

        if (from != null) {
            sql.append(" and coalesce(e.executed_at, s.requested_at) >= :fromDateTime");
        }
        if (to != null) {
            sql.append(" and coalesce(e.executed_at, s.requested_at) <= :toDateTime");
        }
        if (status != null) {
            sql.append(" and s.status = :statusFilter");
        }
        if (ticker != null) {
            sql.append(" and upper(etf.ticker) = :tickerFilter ");
        }
        if (cursorDateTime != null && cursorSweepExecutionId != null) {
            sql.append("""
                     and (
                        coalesce(e.executed_at, s.requested_at) < :cursorDateTime
                        or (
                            coalesce(e.executed_at, s.requested_at) = :cursorDateTime
                            and s.sweep_execution_id < :cursorSweepExecutionId
                        )
                    )
                    """);
        }

        sql.append("""
                order by coalesce(e.executed_at, s.requested_at) desc, s.sweep_execution_id desc
                """);

        Query query = entityManager.createNativeQuery(sql.toString());
        query.setParameter("accountUuid", accountUuid.toString());
        query.setParameter("userUuid", userUuid.toString());

        if (from != null) {
            query.setParameter("fromDateTime", from);
        }
        if (to != null) {
            query.setParameter("toDateTime", to);
        }
        if (status != null) {
            query.setParameter("statusFilter", status.name());
        }
        if (ticker != null) {
            query.setParameter("tickerFilter", ticker);
        }
        if (cursorDateTime != null && cursorSweepExecutionId != null) {
            query.setParameter("cursorDateTime", cursorDateTime);
            query.setParameter("cursorSweepExecutionId", cursorSweepExecutionId);
        }

        query.setMaxResults(limit);

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();
        List<AutoInvestExecutionHistoryView> histories = new ArrayList<>(rows.size());
        for (Object[] row : rows) {
            histories.add(new AutoInvestExecutionHistoryView(
                    toLong(row[0]),
                    toLong(row[1]),
                    toLong(row[2]),
                    (String) row[3],
                    (String) row[4],
                    AutoInvestExecutionStatus.valueOf((String) row[5]),
                    toLong(row[6]),
                    toBigDecimal(row[7]),
                    toBigDecimal(row[8]),
                    toBigDecimal(row[9]),
                    toBigDecimal(row[10]),
                    toLocalDateTime(row[11]),
                    toLocalDateTime(row[12]),
                    (String) row[13],
                    (String) row[14],
                    toLocalDateTime(row[15])
            ));
        }
        return histories;
    }

    private Long toLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        return new BigDecimal(value.toString());
    }

    private LocalDateTime toLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }
        if (value instanceof Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        throw new IllegalArgumentException("Unsupported datetime value: " + value);
    }
}
