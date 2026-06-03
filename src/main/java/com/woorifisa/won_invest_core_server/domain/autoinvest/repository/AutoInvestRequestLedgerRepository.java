package com.woorifisa.won_invest_core_server.domain.autoinvest.repository;

import com.woorifisa.won_invest_core_server.domain.autoinvest.model.AutoInvestRequestLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AutoInvestRequestLedgerRepository extends JpaRepository<AutoInvestRequestLedger, Long> {
    Optional<AutoInvestRequestLedger> findByIdempotencyKey(String idempotencyKey);
}
