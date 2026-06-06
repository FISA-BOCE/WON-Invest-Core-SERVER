package com.woorifisa.won_invest_core_server.domain.autoinvest.repository;

import com.woorifisa.won_invest_core_server.domain.autoinvest.model.InvestAccountEtfHolding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvestAccountEtfHoldingRepository extends JpaRepository<InvestAccountEtfHolding, Long> {
    Optional<InvestAccountEtfHolding> findByInvestAccountInvestAccountUuidAndEtfEtfId(UUID investAccountUuid, Long etfId);

    List<InvestAccountEtfHolding> findByInvestAccountInvestAccountUuidAndHoldingQuantityGreaterThanOrderByEtfHoldingIdAsc(
            UUID investAccountUuid,
            BigDecimal holdingQuantity
    );
}
