package com.woorifisa.won_invest_core_server.domain.account.service;

import com.woorifisa.won_invest_core_server.domain.account.dto.request.CreateInvestAccountRequest;
import com.woorifisa.won_invest_core_server.domain.account.dto.response.CreateInvestAccountResponse;
import com.woorifisa.won_invest_core_server.domain.account.exception.InvestAccountErrorCode;
import com.woorifisa.won_invest_core_server.domain.account.model.InvestChnAccount;
import com.woorifisa.won_invest_core_server.domain.account.model.InvestCustomer;
import com.woorifisa.won_invest_core_server.domain.account.model.InvstConnectedStatus;
import com.woorifisa.won_invest_core_server.domain.account.model.UserMapping;
import com.woorifisa.won_invest_core_server.domain.account.repository.InvestAccountRepository;
import com.woorifisa.won_invest_core_server.domain.account.repository.InvestChnAccountRepository;
import com.woorifisa.won_invest_core_server.domain.account.repository.InvestCustomerRepository;
import com.woorifisa.won_invest_core_server.domain.account.repository.UserMappingRepository;
import com.woorifisa.won_invest_core_server.global.exception.handler.BusinessException;
import com.woorifisa.won_invest_core_server.global.util.CryptoUtil;
import com.woorifisa.won_invest_core_server.global.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class InvestAccountServiceTest {

    @Mock private InvestCustomerRepository investCustomerRepository;
    @Mock private InvestAccountRepository investAccountRepository;
    @Mock private UserMappingRepository userMappingRepository;
    @Mock private InvestChnAccountRepository investChnAccountRepository;
    @Mock private JwtUtil jwtUtil;
    @Mock private CryptoUtil cryptoUtil;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private InvestAccountService investAccountService;

    private static final String AUTH_HEADER = "Bearer test.jwt.token";
    private static final UUID USER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private CreateInvestAccountRequest validRequest() {
        return new CreateInvestAccountRequest(
                "010-1234-5678",
                "홍길동",
                "pass1234!",
                "pass1234!",
                "hong@example.com",
                List.of("INVEST_BASIC", "INVEST_AUTO")
        );
    }

    @Test
    @DisplayName("정상 계좌 개설 성공")
    void openNewInvestAccount_success() {
        // given
        CreateInvestAccountRequest request = validRequest();
        UserMapping userMapping = UserMapping.createNew(USER_UUID);

        given(jwtUtil.extractUserUuid(AUTH_HEADER)).willReturn(USER_UUID);
        given(userMappingRepository.findByUserUuid(USER_UUID)).willReturn(Optional.of(userMapping));
        given(cryptoUtil.encrypt("010-1234-5678")).willReturn("enc_tel");
        given(cryptoUtil.encrypt("hong@example.com")).willReturn("enc_email");
        given(passwordEncoder.encode("pass1234!")).willReturn("$2a$10$dummy_bcrypt_hash");
        given(investCustomerRepository.save(any(InvestCustomer.class))).willAnswer(inv -> inv.getArgument(0));
        given(investChnAccountRepository.save(any(InvestChnAccount.class))).willAnswer(inv -> inv.getArgument(0));

        // when
        CreateInvestAccountResponse response = investAccountService.openNewInvestAccount(request, AUTH_HEADER);

        // then
        assertThat(response.investAccountUuid()).isNotNull();
        assertThat(response.accountNoDisplay()).matches("\\d{3}-\\*{3}-\\*{3}\\d{3}");
        assertThat(response.accountStatus()).isEqualTo("ACTIVE");
        assertThat(response.investConnectedStatus()).isEqualTo("CONNECTED");
        assertThat(response.openedAt()).isNotNull();

        assertThat(userMapping.getInvstConnectedStatus()).isEqualTo(InvstConnectedStatus.CONNECTED);
        assertThat(userMapping.getInvestUserUuid()).isNotNull();

        verify(investAccountRepository).save(any());
        verify(investChnAccountRepository).save(any());
    }

    @Test
    @DisplayName("이미 연결된 증권계좌가 존재하면 ACCOUNT_ALREADY_CONNECTED 예외")
    void openNewInvestAccount_alreadyConnected() {
        // given
        CreateInvestAccountRequest request = validRequest();
        UserMapping connectedMapping = UserMapping.createNew(USER_UUID);
        connectedMapping.connect(UUID.randomUUID());

        given(jwtUtil.extractUserUuid(AUTH_HEADER)).willReturn(USER_UUID);
        given(userMappingRepository.findByUserUuid(USER_UUID)).willReturn(Optional.of(connectedMapping));

        // when / then
        assertThatThrownBy(() -> investAccountService.openNewInvestAccount(request, AUTH_HEADER))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestAccountErrorCode.ACCOUNT_ALREADY_CONNECTED));

        verify(investCustomerRepository, never()).save(any());
        verify(investAccountRepository, never()).save(any());
    }

    @Test
    @DisplayName("비밀번호 불일치 시 PASSWORD_MISMATCH 예외")
    void openNewInvestAccount_passwordMismatch() {
        // given
        CreateInvestAccountRequest request = new CreateInvestAccountRequest(
                "010-1234-5678",
                "홍길동",
                "pass1234!",
                "wrong1234!",
                "hong@example.com",
                List.of("INVEST_BASIC")
        );

        // when / then
        assertThatThrownBy(() -> investAccountService.openNewInvestAccount(request, AUTH_HEADER))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestAccountErrorCode.PASSWORD_MISMATCH));

        verify(jwtUtil, never()).extractUserUuid(any());
        verify(userMappingRepository, never()).findByUserUuid(any());
    }

    @Test
    @DisplayName("필수 약관 미동의 시 REQUIRED_TERMS_NOT_AGREED 예외")
    void openNewInvestAccount_requiredTermsNotAgreed() {
        // given
        CreateInvestAccountRequest request = new CreateInvestAccountRequest(
                "010-1234-5678",
                "홍길동",
                "pass1234!",
                "pass1234!",
                "hong@example.com",
                List.of("INVEST_AUTO")
        );
        UserMapping userMapping = UserMapping.createNew(USER_UUID);

        given(jwtUtil.extractUserUuid(AUTH_HEADER)).willReturn(USER_UUID);
        given(userMappingRepository.findByUserUuid(USER_UUID)).willReturn(Optional.of(userMapping));

        // when / then
        assertThatThrownBy(() -> investAccountService.openNewInvestAccount(request, AUTH_HEADER))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(InvestAccountErrorCode.REQUIRED_TERMS_NOT_AGREED));

        verify(investCustomerRepository, never()).save(any());
    }
}