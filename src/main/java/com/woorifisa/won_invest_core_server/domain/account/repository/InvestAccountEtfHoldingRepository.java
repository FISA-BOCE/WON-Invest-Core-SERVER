package com.woorifisa.won_invest_core_server.domain.account.repository;

import com.woorifisa.won_invest_core_server.domain.account.model.InvestAccountEtfHolding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface InvestAccountEtfHoldingRepository extends JpaRepository<InvestAccountEtfHolding, Long> {

    List<InvestAccountEtfHolding> findByInvestAccountUuidAndHoldingQuantityGreaterThanOrderByEtfHoldingIdAsc(
            UUID investAccountUuid,
            BigDecimal holdingQuantity
    );
}
