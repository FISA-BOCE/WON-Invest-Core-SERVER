package com.woorifisa.won_invest_core_server.domain.account.repository;

import com.woorifisa.won_invest_core_server.domain.account.model.InvestCustomer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InvestCustomerRepository extends JpaRepository<InvestCustomer, UUID> {
    Optional<InvestCustomer> findByUserUuid(UUID userUuid);
}