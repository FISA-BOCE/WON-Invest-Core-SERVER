package com.woorifisa.won_invest_core_server.domain.account.repository;

import com.woorifisa.won_invest_core_server.domain.account.model.InvestAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InvestAccountRepository extends JpaRepository<InvestAccount, UUID> {
}
