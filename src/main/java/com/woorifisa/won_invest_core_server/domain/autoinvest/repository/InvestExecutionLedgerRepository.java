package com.woorifisa.won_invest_core_server.domain.autoinvest.repository;

import com.woorifisa.won_invest_core_server.domain.autoinvest.model.InvestExecutionLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvestExecutionLedgerRepository extends JpaRepository<InvestExecutionLedger, Long> {
    Optional<InvestExecutionLedger> findByOrderOrderId(Long orderId);
}
