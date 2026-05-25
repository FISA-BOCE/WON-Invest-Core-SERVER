package com.woorifisa.won_invest_core_server.domain.account.repository;

import com.woorifisa.won_invest_core_server.domain.account.model.UserMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserMappingRepository extends JpaRepository<UserMapping, Long> {
    Optional<UserMapping> findByUserUuid(UUID userUuid);
}