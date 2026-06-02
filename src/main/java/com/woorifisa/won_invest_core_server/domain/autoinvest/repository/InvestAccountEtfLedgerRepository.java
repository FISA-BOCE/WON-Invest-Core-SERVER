package com.woorifisa.won_invest_core_server.domain.autoinvest.repository;

import com.woorifisa.won_invest_core_server.domain.autoinvest.model.InvestAccountEtfLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InvestAccountEtfLedgerRepository extends JpaRepository<InvestAccountEtfLedger, Long> {
}
