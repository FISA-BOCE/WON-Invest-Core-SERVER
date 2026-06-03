package com.woorifisa.won_invest_core_server.domain.autoinvest.repository;

import com.woorifisa.won_invest_core_server.domain.autoinvest.model.AutoInvestExecutionLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AutoInvestExecutionLedgerRepository extends JpaRepository<AutoInvestExecutionLedger, Long> {
    Optional<AutoInvestExecutionLedger> findByIdempotencyKey(String idempotencyKey);
}
