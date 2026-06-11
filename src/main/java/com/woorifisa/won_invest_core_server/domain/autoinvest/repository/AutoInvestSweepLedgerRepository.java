package com.woorifisa.won_invest_core_server.domain.autoinvest.repository;

import com.woorifisa.won_invest_core_server.domain.autoinvest.model.AutoInvestSweepLedger;
import com.woorifisa.won_invest_core_server.domain.autoinvest.model.enums.AutoInvestExecutionStatus;
import com.woorifisa.won_invest_core_server.domain.autoinvest.service.projection.AdminAutoInvestExecutionView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AutoInvestSweepLedgerRepository extends JpaRepository<AutoInvestSweepLedger, Long>, AutoInvestSweepLedgerQueryRepository {
    Optional<AutoInvestSweepLedger> findByIdempotencyKey(String idempotencyKey);

    @Query("""
            select new com.woorifisa.won_invest_core_server.domain.autoinvest.service.projection.AdminAutoInvestExecutionView(
                l.sweepExecutionId,
                l.sweepRequestId,
                l.userUuid,
                l.etfId,
                e.ticker,
                l.status,
                l.failureCode,
                l.failureMessage,
                l.orderId,
                o.orderStatus,
                l.executionLedgerId,
                l.fxRateSnapshot,
                l.requestedAt,
                l.completedAt,
                l.createdAt,
                l.updatedAt
            )
            from AutoInvestSweepLedger l
            left join AutoInvestOrderLedger o on o.orderId = l.orderId
            left join InvestEtfProduct e on e.etfId = l.etfId
            where (:status is null or l.status = :status)
              and (:userUuid is null or l.userUuid = :userUuid)
              and (:executionId is null or l.sweepExecutionId = :executionId)
              and (:sweepRequestId is null or l.sweepRequestId = :sweepRequestId)
              and (:ticker is null or lower(e.ticker) like lower(concat('%', :ticker, '%')))
            order by l.sweepExecutionId desc
            """)
    Page<AdminAutoInvestExecutionView> findAdminExecutions(
            @Param("status") AutoInvestExecutionStatus status,
            @Param("userUuid") UUID userUuid,
            @Param("executionId") Long executionId,
            @Param("sweepRequestId") Long sweepRequestId,
            @Param("ticker") String ticker,
            Pageable pageable
    );

    @Query("""
            select count(l)
            from AutoInvestSweepLedger l
            left join InvestEtfProduct e on e.etfId = l.etfId
            where (:status is null or l.status = :status)
              and (:userUuid is null or l.userUuid = :userUuid)
              and (:executionId is null or l.sweepExecutionId = :executionId)
              and (:sweepRequestId is null or l.sweepRequestId = :sweepRequestId)
              and (:ticker is null or lower(e.ticker) like lower(concat('%', :ticker, '%')))
            """)
    long countAdminExecutions(
            @Param("status") AutoInvestExecutionStatus status,
            @Param("userUuid") UUID userUuid,
            @Param("executionId") Long executionId,
            @Param("sweepRequestId") Long sweepRequestId,
            @Param("ticker") String ticker
    );

    @Query("""
            select count(l)
            from AutoInvestSweepLedger l
            left join InvestEtfProduct e on e.etfId = l.etfId
            where l.fxRateSnapshot is not null
              and (:userUuid is null or l.userUuid = :userUuid)
              and (:executionId is null or l.sweepExecutionId = :executionId)
              and (:sweepRequestId is null or l.sweepRequestId = :sweepRequestId)
              and (:ticker is null or lower(e.ticker) like lower(concat('%', :ticker, '%')))
            """)
    long countAdminExchangeCompletedExecutions(
            @Param("userUuid") UUID userUuid,
            @Param("executionId") Long executionId,
            @Param("sweepRequestId") Long sweepRequestId,
            @Param("ticker") String ticker
    );

    @Query("""
            select count(l)
            from AutoInvestSweepLedger l
            left join AutoInvestOrderLedger o on o.orderId = l.orderId
            left join InvestEtfProduct e on e.etfId = l.etfId
            where o.orderStatus = com.woorifisa.won_invest_core_server.domain.autoinvest.model.enums.OrderStatus.FAILED
              and (:userUuid is null or l.userUuid = :userUuid)
              and (:executionId is null or l.sweepExecutionId = :executionId)
              and (:sweepRequestId is null or l.sweepRequestId = :sweepRequestId)
              and (:ticker is null or lower(e.ticker) like lower(concat('%', :ticker, '%')))
            """)
    long countAdminOrderFailedExecutions(
            @Param("userUuid") UUID userUuid,
            @Param("executionId") Long executionId,
            @Param("sweepRequestId") Long sweepRequestId,
            @Param("ticker") String ticker
    );
}
