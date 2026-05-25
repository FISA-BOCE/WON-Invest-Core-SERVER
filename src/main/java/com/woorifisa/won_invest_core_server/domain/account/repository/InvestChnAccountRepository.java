package com.woorifisa.won_invest_core_server.domain.account.repository;

import com.woorifisa.won_invest_core_server.domain.account.model.InvestChnAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InvestChnAccountRepository extends JpaRepository<InvestChnAccount, Long> {
    Optional<InvestChnAccount> findByInvestAccountUuid(UUID investAccountUuid);
}