package com.woorifisa.won_invest_core_server.domain.account.service;

import com.woorifisa.won_invest_core_server.domain.account.exception.InvestAccountErrorCode;
import com.woorifisa.won_invest_core_server.domain.account.model.InvestAccount;
import com.woorifisa.won_invest_core_server.domain.account.model.enums.AccountStatus;
import com.woorifisa.won_invest_core_server.domain.account.repository.InvestAccountRepository;
import com.woorifisa.won_invest_core_server.global.exception.code.ErrorCode;
import com.woorifisa.won_invest_core_server.global.exception.handler.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class InvestAccountQuerySupport {

    private final InvestAccountRepository investAccountRepository;

    public InvestAccount getAccessibleActiveAccount(UUID accountUuid, UUID userUuid, ErrorCode inactiveAccountErrorCode) {
        InvestAccount account = investAccountRepository.findById(accountUuid)
                .orElseThrow(() -> new BusinessException(InvestAccountErrorCode.ACCOUNT_NOT_FOUND));

        if (!account.getUserUuid().equals(userUuid)) {
            throw new BusinessException(InvestAccountErrorCode.ACCOUNT_ACCESS_DENIED);
        }

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException(inactiveAccountErrorCode);
        }

        return account;
    }
}
