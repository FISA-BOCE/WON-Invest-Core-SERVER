package com.woorifisa.won_invest_core_server.domain.account.service;

import com.woorifisa.won_invest_core_server.domain.account.dto.request.CreateInvestAccountRequest;
import com.woorifisa.won_invest_core_server.domain.account.dto.response.CreateInvestAccountResponse;
import com.woorifisa.won_invest_core_server.domain.account.exception.InvestAccountErrorCode;
import com.woorifisa.won_invest_core_server.domain.account.model.AccountStatus;
import com.woorifisa.won_invest_core_server.domain.account.model.InvestAccount;
import com.woorifisa.won_invest_core_server.domain.account.model.InvestConnectedStatus;
import com.woorifisa.won_invest_core_server.domain.account.model.InvestUser;
import com.woorifisa.won_invest_core_server.domain.account.model.RequiredTerms;
import com.woorifisa.won_invest_core_server.domain.account.repository.InvestAccountRepository;
import com.woorifisa.won_invest_core_server.domain.account.repository.InvestUserRepository;
import com.woorifisa.won_invest_core_server.global.exception.handler.BusinessException;
import com.woorifisa.won_invest_core_server.global.util.AccountMaskUtil;
import com.woorifisa.won_invest_core_server.global.util.CryptoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class InvestAccountService {

    private final InvestUserRepository investUserRepository;
    private final InvestAccountRepository investAccountRepository;
    private final CryptoUtil cryptoUtil;
    private final PasswordEncoder passwordEncoder;

    public CreateInvestAccountResponse openNewInvestAccount(
            CreateInvestAccountRequest request,
            UUID userUuid
    ) {
        // 1. 약관 동의 검증 (INVEST_BASIC 필수)
        validateRequiredTerms(request.agreedTerms());

        // 2. 이미 개설된 계좌 존재 여부 확인
        if (investUserRepository.findByUserUuid(userUuid).isPresent()) {
            throw new BusinessException(InvestAccountErrorCode.ACCOUNT_ALREADY_CONNECTED);
        }

        // 3. 증권망 고객 원본 정보 생성 (tel_enc, email_enc 암호화)
        UUID investUserUuid = UUID.randomUUID();
        InvestUser investUser = investUserRepository.save(
                InvestUser.builder()
                        .investUserUuid(investUserUuid)
                        .userUuid(userUuid)
                        .customerName(request.customerName())
                        .telEnc(cryptoUtil.encrypt(request.phoneNumber()))
                        .emailEnc(cryptoUtil.encrypt(request.email()))
                        .build()
        );

        // 4. 증권 계좌 원본 정보 생성 + invest_account_uuid 발급 (accountNo 충돌 시 최대 3회 재시도)
        LocalDateTime openedAt = LocalDateTime.now();
        UUID investAccountUuid = UUID.randomUUID();
        String accountNo = generateAccountNo();
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                saveInvestAccount(investAccountUuid, investUser, userUuid, request.accountPassword(), accountNo, openedAt);
                break;
            } catch (DataIntegrityViolationException e) {
                if (!isAccountNoDuplicate(e) || attempt == 2) throw e;
                investAccountUuid = UUID.randomUUID();
                accountNo = generateAccountNo();
            }
        }

        // 5. 응답 반환
        return new CreateInvestAccountResponse(
                investAccountUuid,
                investUserUuid,
                AccountMaskUtil.mask(accountNo),
                AccountStatus.ACTIVE.name(),
                InvestConnectedStatus.CONNECTED.name(),
                openedAt
        );
    }

    private void validateRequiredTerms(List<String> agreedTerms) {
        if (agreedTerms == null || !agreedTerms.contains(RequiredTerms.INVEST_BASIC.name())) {
            throw new BusinessException(InvestAccountErrorCode.REQUIRED_TERMS_NOT_AGREED);
        }
    }

    private void saveInvestAccount(UUID investAccountUuid, InvestUser investUser, UUID userUuid,
                                    String accountPassword, String accountNo, LocalDateTime openedAt) {
        investAccountRepository.saveAndFlush(InvestAccount.builder()
                .investAccountUuid(investAccountUuid)
                .investUser(investUser)
                .userUuid(userUuid)
                .accountPasswordEnc(passwordEncoder.encode(accountPassword))
                .accountNo(accountNo)
                .accountStatus(AccountStatus.ACTIVE)
                .openedAt(openedAt)
                .build());
    }

    private boolean isAccountNoDuplicate(DataIntegrityViolationException e) {
        Throwable cause = e.getMostSpecificCause();
        String msg = cause != null ? cause.getMessage() : null;
        return msg != null && msg.contains("account_no");
    }

    private String generateAccountNo() {
        return String.format("%012d",
                ThreadLocalRandom.current().nextLong(0L, 1_000_000_000_000L));
    }
}
