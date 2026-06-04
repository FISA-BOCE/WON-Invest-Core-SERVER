package com.woorifisa.won_invest_core_server.domain.account.repository;

import com.woorifisa.won_invest_core_server.domain.account.model.InvestExecutionLedger;
import com.woorifisa.won_invest_core_server.domain.account.model.InvestOrderType;
import com.woorifisa.won_invest_core_server.domain.account.service.projection.RecentExecutionView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface InvestExecutionLedgerRepository extends JpaRepository<InvestExecutionLedger, Long> {

    @Query("""
            select new com.woorifisa.won_invest_core_server.domain.account.service.projection.RecentExecutionView(
                e.executedAt,
                e.ticker,
                e.executionQuantity,
                o.orderType
            )
            from InvestExecutionLedger e
            join InvestOrderLedger o on o.orderId = e.orderId
            where e.investAccountUuid = :accountUuid
              and o.orderType in :buyOrderTypes
            order by e.executedAt desc
            """)
    List<RecentExecutionView> findRecentExecutionsByAccountUuid(
            @Param("accountUuid") UUID accountUuid,
            @Param("buyOrderTypes") List<String> buyOrderTypes,
            Pageable pageable
    );
}
