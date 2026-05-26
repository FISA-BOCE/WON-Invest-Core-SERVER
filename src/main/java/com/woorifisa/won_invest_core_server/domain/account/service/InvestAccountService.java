package com.woorifisa.won_invest_core_server.domain.account.service;

import com.woorifisa.won_invest_core_server.domain.account.dto.request.CreateInvestAccountRequest;
import com.woorifisa.won_invest_core_server.domain.account.dto.response.CreateInvestAccountResponse;
import com.woorifisa.won_invest_core_server.domain.account.exception.InvestAccountErrorCode;
import com.woorifisa.won_invest_core_server.domain.account.model.AccountStatus;
import com.woorifisa.won_invest_core_server.domain.account.model.InvestAccount;
import com.woorifisa.won_invest_core_server.domain.account.model.InvestChnAccount;
import com.woorifisa.won_invest_core_server.domain.account.model.InvestCustomer;
import com.woorifisa.won_invest_core_server.domain.account.model.InvstConnectedStatus;
import com.woorifisa.won_invest_core_server.domain.account.model.UserMapping;
import com.woorifisa.won_invest_core_server.domain.account.repository.InvestAccountRepository;
import com.woorifisa.won_invest_core_server.domain.account.repository.InvestChnAccountRepository;
import com.woorifisa.won_invest_core_server.domain.account.repository.InvestCustomerRepository;
import com.woorifisa.won_invest_core_server.domain.account.repository.UserMappingRepository;
import com.woorifisa.won_invest_core_server.global.exception.handler.BusinessException;
import com.woorifisa.won_invest_core_server.global.util.AccountMaskUtil;
import com.woorifisa.won_invest_core_server.global.util.CryptoUtil;
import com.woorifisa.won_invest_core_server.global.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class InvestAccountService {

    private final InvestCustomerRepository investCustomerRepository;
    private final InvestAccountRepository investAccountRepository;
    private final UserMappingRepository userMappingRepository;
    private final InvestChnAccountRepository investChnAccountRepository;
    private final JwtUtil jwtUtil;
    private final CryptoUtil cryptoUtil;
    private final PasswordEncoder passwordEncoder;

    public CreateInvestAccountResponse openNewInvestAccount(
            CreateInvestAccountRequest request,
            String authorizationHeader
    ) {
        // 1. 입력값 검증
        validatePasswordMatch(request.accountPassword(), request.accountPasswordConfirm());

        // 2. JWT 유효성 확인 및 user_uuid 추출
        UUID userUuid = jwtUtil.extractUserUuid(authorizationHeader);

        // 3. 통합 사용자 매핑 CONNECTED 중복 확인
        UserMapping userMapping = findOrCreateUserMapping(userUuid);
        if (userMapping.getInvstConnectedStatus() == InvstConnectedStatus.CONNECTED) {
            throw new BusinessException(InvestAccountErrorCode.ACCOUNT_ALREADY_CONNECTED);
        }

        // 4. 약관 동의 검증 (INVEST_BASIC 필수)
        validateRequiredTerms(request.agreedTerms());

        // 5. 증권망 고객 원본 정보 생성 (tel_enc, email_enc 암호화)
        UUID investUserUuid = UUID.randomUUID();
        InvestCustomer savedCustomer = investCustomerRepository.save(
                InvestCustomer.builder()
                        .investUserUuid(investUserUuid)
                        .userUuid(userUuid)
                        .customerName(request.customerName())
                        .telEnc(cryptoUtil.encrypt(request.phoneNumber()))
                        .emailEnc(cryptoUtil.encrypt(request.email()))
                        .build()
        );

        // 6. 증권 계좌 원본 정보 생성 + invest_account_uuid 발급
        UUID investAccountUuid = UUID.randomUUID();
        String accountNo = generateAccountNo();
        Instant openedAt = Instant.now();
        InvestAccount account = InvestAccount.builder()
                .investAccountUuid(investAccountUuid)
                .investCustomer(savedCustomer)
                .userUuid(userUuid)
                .accountPasswordEnc(passwordEncoder.encode(request.accountPassword()))
                .accountNo(accountNo)
                .accountStatus(AccountStatus.ACTIVE)
                .openedAt(openedAt)
                .build();
        investAccountRepository.save(account);

        // 7. 통합 사용자 매핑 invest_connected_status = CONNECTED 업데이트 (dirty checking)
        userMapping.connect(investUserUuid);

        // 8. 채널계 조회용 증권 계좌 동기화 (account_no_display 마스킹)
        String accountNoDisplay = AccountMaskUtil.mask(accountNo);
        InvestChnAccount chnAccount = InvestChnAccount.builder()
                .investAccountUuid(investAccountUuid)
                .userUuid(userUuid)
                .accountNoDisplay(accountNoDisplay)
                .accountStatus(AccountStatus.ACTIVE)
                .build();
        investChnAccountRepository.save(chnAccount);

        // 9. 응답 반환
        return new CreateInvestAccountResponse(
                investAccountUuid,
                accountNoDisplay,
                AccountStatus.ACTIVE.name(),
                InvstConnectedStatus.CONNECTED.name(),
                openedAt
        );
    }

    private void validatePasswordMatch(String password, String passwordConfirm) {
        if (!password.equals(passwordConfirm)) {
            throw new BusinessException(InvestAccountErrorCode.PASSWORD_MISMATCH);
        }
    }

    private void validateRequiredTerms(List<String> agreedTerms) {
        if (agreedTerms == null || !agreedTerms.contains("INVEST_BASIC")) {
            throw new BusinessException(InvestAccountErrorCode.REQUIRED_TERMS_NOT_AGREED);
        }
    }

    private UserMapping findOrCreateUserMapping(UUID userUuid) {
        return userMappingRepository.findByUserUuid(userUuid)
                .orElseGet(() -> userMappingRepository.save(UserMapping.createNew(userUuid)));
    }

    private String generateAccountNo() {
        return String.format("%012d",
                ThreadLocalRandom.current().nextLong(0L, 1_000_000_000_000L));
    }
}