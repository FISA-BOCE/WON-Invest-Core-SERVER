package com.woorifisa.won_invest_core_server.domain.account.repository;

import com.woorifisa.won_invest_core_server.domain.account.model.InvestOrderLedger;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestOrderLedgerRepository extends JpaRepository<InvestOrderLedger, Long> {
}
