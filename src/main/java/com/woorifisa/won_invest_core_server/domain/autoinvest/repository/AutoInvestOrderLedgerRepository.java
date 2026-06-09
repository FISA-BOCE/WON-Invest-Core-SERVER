package com.woorifisa.won_invest_core_server.domain.autoinvest.repository;

import com.woorifisa.won_invest_core_server.domain.autoinvest.model.InvestOrderLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AutoInvestOrderLedgerRepository extends JpaRepository<InvestOrderLedger, Long > {
    Optional<InvestOrderLedger> findByIdempotencyKey(String idempotencyKey);
    Optional<InvestOrderLedger> findBySweepId(Long sweepId);
}
