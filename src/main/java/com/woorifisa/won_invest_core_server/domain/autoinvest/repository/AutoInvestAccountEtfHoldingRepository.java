package com.woorifisa.won_invest_core_server.domain.autoinvest.repository;

import com.woorifisa.won_invest_core_server.domain.autoinvest.model.InvestAccountEtfHolding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AutoInvestAccountEtfHoldingRepository extends JpaRepository<InvestAccountEtfHolding, Long> {
    Optional<InvestAccountEtfHolding> findByInvestAccountInvestAccountUuidAndEtfEtfId(UUID investAccountUuid, Long etfId);
}
