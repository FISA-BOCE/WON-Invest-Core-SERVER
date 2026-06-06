package com.woorifisa.won_invest_core_server.domain.account.api;

import com.woorifisa.won_invest_core_server.domain.account.model.InvestAccount;
import com.woorifisa.won_invest_core_server.domain.account.model.InvestAccountEtfHolding;
import com.woorifisa.won_invest_core_server.domain.account.model.InvestExecutionLedger;
import com.woorifisa.won_invest_core_server.domain.account.model.InvestOrderLedger;
import com.woorifisa.won_invest_core_server.domain.account.model.InvestUser;
import com.woorifisa.won_invest_core_server.domain.account.model.enums.AccountStatus;
import com.woorifisa.won_invest_core_server.domain.account.repository.InvestAccountEtfHoldingRepository;
import com.woorifisa.won_invest_core_server.domain.account.repository.InvestAccountRepository;
import com.woorifisa.won_invest_core_server.domain.account.repository.InvestExecutionLedgerRepository;
import com.woorifisa.won_invest_core_server.domain.account.repository.InvestOrderLedgerRepository;
import com.woorifisa.won_invest_core_server.domain.account.repository.InvestUserRepository;
import com.woorifisa.won_invest_core_server.domain.etf.model.InvestEtfProduct;
import com.woorifisa.won_invest_core_server.domain.etf.model.enums.EtfCurrency;
import com.woorifisa.won_invest_core_server.domain.etf.model.enums.EtfProductStatus;
import com.woorifisa.won_invest_core_server.domain.etf.repository.InvestEtfProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class InternalInvestAccountEtfApiTest {

    private static final String SERVICE_ID_HEADER = "X-Service-ID";
    private static final String API_KEY_HEADER = "X-Internal-Api-Key";
    private static final String USER_UUID_HEADER = "X-User-UUID";
    private static final String SERVICE_ID = "won-invest-channel";
    private static final String API_KEY = "test-internal-api-key";
    private static final UUID USER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_USER_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID INVEST_USER_UUID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID ACCOUNT_UUID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InvestExecutionLedgerRepository investExecutionLedgerRepository;

    @Autowired
    private InvestOrderLedgerRepository investOrderLedgerRepository;

    @Autowired
    private InvestAccountEtfHoldingRepository investAccountEtfHoldingRepository;

    @Autowired
    private InvestEtfProductRepository investEtfProductRepository;

    @Autowired
    private InvestAccountRepository investAccountRepository;

    @Autowired
    private InvestUserRepository investUserRepository;

    @AfterEach
    void tearDown() {
        investExecutionLedgerRepository.deleteAll();
        investOrderLedgerRepository.deleteAll();
        investAccountEtfHoldingRepository.deleteAll();
        investEtfProductRepository.deleteAll();
        investAccountRepository.deleteAll();
        investUserRepository.deleteAll();
    }

    @Test
    @DisplayName("정상 조회 시 보유 ETF, 최근 매수 체결 3건, 집계 금액을 반환한다")
    void getAccountEtfDetails_success() throws Exception {
        seedAccount(USER_UUID, AccountStatus.ACTIVE);
        Long vooEtfId = seedEtfProduct("VOO", "Vanguard S&P 500 ETF");
        Long qqqEtfId = seedEtfProduct("QQQ", "Invesco QQQ Trust");
        Long spyEtfId = seedEtfProduct("SPY", "SPDR S&P 500 ETF Trust");
        seedHolding(1L, vooEtfId, "VOO", "2.50000000", "100.0000", "250.0000", "300.0000", null, null);
        seedHolding(2L, qqqEtfId, "QQQ", "1.00000000", "200.0000", "200.0000", "260.0000", null, null);
        seedHolding(3L, spyEtfId, "SPY", "0.00000000", "300.0000", "0.0000", "0.0000", null, null);

        seedOrderAndExecution(1L, 11L, "AUTO_BUY", "VOO", "0.50000000", LocalDateTime.of(2026, 6, 4, 12, 0));
        seedOrderAndExecution(2L, 12L, "AUTO_BUY", "QQQ", "1.00000000", LocalDateTime.of(2026, 6, 4, 11, 0));
        seedOrderAndExecution(3L, 13L, "SELL", "VOO", "0.30000000", LocalDateTime.of(2026, 6, 4, 10, 0));
        seedOrderAndExecution(4L, 14L, "AUTO_BUY", "SPY", "0.70000000", LocalDateTime.of(2026, 6, 4, 9, 0));
        seedOrderAndExecution(5L, 15L, "BUY", "DIA", "0.20000000", LocalDateTime.of(2026, 6, 4, 8, 0));

        mockMvc.perform(get("/internal/invest/accounts/{accountUuid}/etfs", ACCOUNT_UUID)
                        .header(SERVICE_ID_HEADER, SERVICE_ID)
                        .header(API_KEY_HEADER, API_KEY)
                        .header(USER_UUID_HEADER, USER_UUID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("INVEST_200_001"))
                .andExpect(jsonPath("$.message").value("보유 ETF 상세 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.baseDate").value(LocalDate.now(ZoneId.of("Asia/Seoul")).toString()))
                .andExpect(jsonPath("$.data.totalEvaluationAmount").value(560.0000))
                .andExpect(jsonPath("$.data.profitLossAmount").value(110.0000))
                .andExpect(jsonPath("$.data.profitLossRate").value(24.444444))
                .andExpect(jsonPath("$.data.holdings.length()").value(2))
                .andExpect(jsonPath("$.data.holdings[0].etfId").value(vooEtfId))
                .andExpect(jsonPath("$.data.holdings[0].etfName").value("Vanguard S&P 500 ETF"))
                .andExpect(jsonPath("$.data.holdings[0].ticker").value("VOO"))
                .andExpect(jsonPath("$.data.holdings[0].profitLossAmount").value(50.0000))
                .andExpect(jsonPath("$.data.holdings[0].profitLossRate").value(20.000000))
                .andExpect(jsonPath("$.data.recentExecutions.length()").value(3))
                .andExpect(jsonPath("$.data.recentExecutions[0].ticker").value("VOO"))
                .andExpect(jsonPath("$.data.recentExecutions[0].executionType").value("AUTO_BUY"))
                .andExpect(jsonPath("$.data.recentExecutions[0].executedAt").value("2026-06-04T12:00:00+09:00"))
                .andExpect(jsonPath("$.data.recentExecutions[1].executionType").value("AUTO_BUY"))
                .andExpect(jsonPath("$.data.recentExecutions[1].executedAt").value("2026-06-04T11:00:00+09:00"))
                .andExpect(jsonPath("$.data.recentExecutions[2].executionType").value("AUTO_BUY"))
                .andExpect(jsonPath("$.data.recentExecutions[2].executedAt").value("2026-06-04T09:00:00+09:00"));
    }

    @Test
    @DisplayName("계좌가 없으면 404를 반환한다")
    void getAccountEtfDetails_accountNotFound() throws Exception {
        mockMvc.perform(get("/internal/invest/accounts/{accountUuid}/etfs", ACCOUNT_UUID)
                        .header(SERVICE_ID_HEADER, SERVICE_ID)
                        .header(API_KEY_HEADER, API_KEY)
                        .header(USER_UUID_HEADER, USER_UUID.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("INVEST_404_001"));
    }

    @Test
    @DisplayName("본인 계좌가 아니면 403을 반환한다")
    void getAccountEtfDetails_accessDenied() throws Exception {
        seedAccount(USER_UUID, AccountStatus.ACTIVE);

        mockMvc.perform(get("/internal/invest/accounts/{accountUuid}/etfs", ACCOUNT_UUID)
                        .header(SERVICE_ID_HEADER, SERVICE_ID)
                        .header(API_KEY_HEADER, API_KEY)
                        .header(USER_UUID_HEADER, OTHER_USER_UUID.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("INVEST_403_001"));
    }

    @Test
    @DisplayName("비활성 계좌면 400을 반환한다")
    void getAccountEtfDetails_inactiveAccount() throws Exception {
        seedAccount(USER_UUID, AccountStatus.INACTIVE);

        mockMvc.perform(get("/internal/invest/accounts/{accountUuid}/etfs", ACCOUNT_UUID)
                        .header(SERVICE_ID_HEADER, SERVICE_ID)
                        .header(API_KEY_HEADER, API_KEY)
                        .header(USER_UUID_HEADER, USER_UUID.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVEST_400_005"));
    }

    @Test
    @DisplayName("내부 인증 헤더가 없으면 401을 반환한다")
    void getAccountEtfDetails_missingInternalAuthHeader() throws Exception {
        mockMvc.perform(get("/internal/invest/accounts/{accountUuid}/etfs", ACCOUNT_UUID)
                        .header(USER_UUID_HEADER, USER_UUID.toString()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_401_001"));
    }

    @Test
    @DisplayName("내부 API Key가 올바르지 않으면 401을 반환한다")
    void getAccountEtfDetails_invalidInternalAuthHeader() throws Exception {
        mockMvc.perform(get("/internal/invest/accounts/{accountUuid}/etfs", ACCOUNT_UUID)
                        .header(SERVICE_ID_HEADER, SERVICE_ID)
                        .header(API_KEY_HEADER, "wrong-key")
                        .header(USER_UUID_HEADER, USER_UUID.toString()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH_401_001"));
    }

    @Test
    @DisplayName("X-User-UUID 헤더가 없으면 400을 반환한다")
    void getAccountEtfDetails_missingUserUuidHeader() throws Exception {
        mockMvc.perform(get("/internal/invest/accounts/{accountUuid}/etfs", ACCOUNT_UUID)
                        .header(SERVICE_ID_HEADER, SERVICE_ID)
                        .header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("COM_400_001"));
    }

    @Test
    @DisplayName("총 매수금액이 0이면 총 수익률은 0을 반환한다")
    void getAccountEtfDetails_zeroBuyAmountReturnsZeroRate() throws Exception {
        seedAccount(USER_UUID, AccountStatus.ACTIVE);
        Long vooEtfId = seedEtfProduct("VOO", "Vanguard S&P 500 ETF");
        seedHolding(1L, vooEtfId, "VOO", "1.00000000", "0.0000", "0.0000", "100.0000", null, null);

        mockMvc.perform(get("/internal/invest/accounts/{accountUuid}/etfs", ACCOUNT_UUID)
                        .header(SERVICE_ID_HEADER, SERVICE_ID)
                        .header(API_KEY_HEADER, API_KEY)
                        .header(USER_UUID_HEADER, USER_UUID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.profitLossRate").value(0));
    }

    private void seedAccount(UUID userUuid, AccountStatus accountStatus) {
        InvestUser investUser = investUserRepository.save(InvestUser.builder()
                .investUserUuid(INVEST_USER_UUID)
                .userUuid(userUuid)
                .customerName("홍길동")
                .telEnc("enc-tel")
                .emailEnc("enc-email")
                .build());

        investAccountRepository.save(InvestAccount.builder()
                .investAccountUuid(ACCOUNT_UUID)
                .investUser(investUser)
                .userUuid(userUuid)
                .accountPasswordEnc("encoded")
                .accountNo("123456789012")
                .accountStatus(accountStatus)
                .openedAt(LocalDateTime.of(2026, 6, 1, 9, 0))
                .build());
    }

    private Long seedEtfProduct(String ticker, String etfName) {
        return investEtfProductRepository.save(InvestEtfProduct.builder()
                .externalProvider("KIS")
                .externalEtfId(ticker)
                .ticker(ticker)
                .isin("ISIN-" + ticker)
                .etfName(etfName)
                .market("NASDAQ")
                .currency(EtfCurrency.USD)
                .productStatus(EtfProductStatus.ACTIVE)
                .isFractionalAvailable(true)
                .isTradeAvailable(true)
                .lastSyncedAt(LocalDateTime.of(2026, 6, 4, 7, 0))
                .build()).getEtfId();
    }

    private void seedHolding(
            Long holdingId,
            Long etfId,
            String ticker,
            String holdingQuantity,
            String averageBuyPrice,
            String totalBuyAmount,
            String evaluationAmount,
            String profitLossAmount,
            String profitLossRate
    ) {
        investAccountEtfHoldingRepository.save(InvestAccountEtfHolding.builder()
                .etfHoldingId(holdingId)
                .investAccountUuid(ACCOUNT_UUID)
                .investUserUuid(INVEST_USER_UUID)
                .userUuid(USER_UUID)
                .etfId(etfId)
                .ticker(ticker)
                .holdingQuantity(new BigDecimal(holdingQuantity))
                .averageBuyPrice(new BigDecimal(averageBuyPrice))
                .totalBuyAmount(new BigDecimal(totalBuyAmount))
                .evaluationAmount(new BigDecimal(evaluationAmount))
                .profitLossAmount(profitLossAmount == null ? null : new BigDecimal(profitLossAmount))
                .profitLossRate(profitLossRate == null ? null : new BigDecimal(profitLossRate))
                .build());
    }

    private void seedOrderAndExecution(
            Long orderId,
            Long executionId,
            String orderType,
            String ticker,
            String executionQuantity,
            LocalDateTime executedAt
    ) {
        investOrderLedgerRepository.save(InvestOrderLedger.builder()
                .orderId(orderId)
                .investAccountUuid(ACCOUNT_UUID)
                .userUuid(USER_UUID)
                .investUserUuid(INVEST_USER_UUID)
                .etfId(101L)
                .ticker(ticker)
                .orderType(orderType)
                .orderMethod("MARKET")
                .orderCurrency("USD")
                .orderAmount(new BigDecimal("100.0000"))
                .orderQuantity(new BigDecimal(executionQuantity))
                .referencePrice(new BigDecimal("100.0000"))
                .orderStatus("COMPLETED")
                .orderedAt(executedAt.minusMinutes(1))
                .idempotencyKey("idem-" + orderId)
                .build());

        investExecutionLedgerRepository.save(InvestExecutionLedger.builder()
                .executionId(executionId)
                .orderId(orderId)
                .investAccountUuid(ACCOUNT_UUID)
                .userUuid(USER_UUID)
                .investUserUuid(INVEST_USER_UUID)
                .etfId(101L)
                .ticker(ticker)
                .executionQuantity(new BigDecimal(executionQuantity))
                .executionPrice(new BigDecimal("100.0000"))
                .executionAmount(new BigDecimal("100.0000"))
                .executedAt(executedAt)
                .build());
    }
}
