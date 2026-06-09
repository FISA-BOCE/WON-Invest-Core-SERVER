package com.woorifisa.won_invest_core_server.domain.autoinvest.repository;

import com.woorifisa.won_invest_core_server.domain.autoinvest.model.InvestExecutionLedger;
import com.woorifisa.won_invest_core_server.domain.account.service.projection.RecentExecutionView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AutoInvestExecutionLedgerRepository extends JpaRepository<InvestExecutionLedger, Long> {
    Optional<InvestExecutionLedger> findByOrderOrderId(Long orderId);

    @Query("""
            select new com.woorifisa.won_invest_core_server.domain.account.service.projection.RecentExecutionView(
                e.executedAt,
                e.ticker,
                e.executionQuantity,
                'AUTO_BUY'
            )
            from AutoInvestExecutionLedger e
            where e.investAccount.investAccountUuid = :accountUuid
            order by e.executedAt desc
            """)
    List<RecentExecutionView> findRecentExecutionsByAccountUuid(
            @Param("accountUuid") UUID accountUuid,
            Pageable pageable
    );
}
