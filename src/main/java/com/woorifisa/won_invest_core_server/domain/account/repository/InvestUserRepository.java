package com.woorifisa.won_invest_core_server.domain.account.repository;

import com.woorifisa.won_invest_core_server.domain.account.model.InvestUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InvestUserRepository extends JpaRepository<InvestUser, UUID> {
    Optional<InvestUser> findByUserUuid(UUID userUuid);
}