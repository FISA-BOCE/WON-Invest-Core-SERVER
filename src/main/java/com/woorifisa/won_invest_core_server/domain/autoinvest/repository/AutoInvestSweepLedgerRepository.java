package com.woorifisa.won_invest_core_server.domain.autoinvest.repository;

import com.woorifisa.won_invest_core_server.domain.autoinvest.model.AutoInvestSweepLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AutoInvestSweepLedgerRepository extends JpaRepository<AutoInvestSweepLedger, Long> {
    Optional<AutoInvestSweepLedger> findByIdempotencyKey(String idempotencyKey);
}
