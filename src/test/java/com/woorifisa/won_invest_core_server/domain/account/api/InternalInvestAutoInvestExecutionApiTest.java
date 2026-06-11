package com.woorifisa.won_invest_core_server.domain.account.api;

import com.woorifisa.won_invest_core_server.domain.account.model.InvestAccount;
import com.woorifisa.won_invest_core_server.domain.account.model.InvestUser;
import com.woorifisa.won_invest_core_server.domain.account.model.enums.AccountStatus;
import com.woorifisa.won_invest_core_server.domain.account.repository.InvestAccountRepository;
import com.woorifisa.won_invest_core_server.domain.account.repository.InvestUserRepository;
import com.woorifisa.won_invest_core_server.domain.autoinvest.dto.request.AutoInvestExecutionRequest;
import com.woorifisa.won_invest_core_server.domain.autoinvest.exception.code.AutoInvestFailureCode;
import com.woorifisa.won_invest_core_server.domain.autoinvest.model.AutoInvestSweepLedger;
import com.woorifisa.won_invest_core_server.domain.autoinvest.model.InvestExecutionLedger;
import com.woorifisa.won_invest_core_server.domain.autoinvest.model.InvestOrderLedger;
import com.woorifisa.won_invest_core_server.domain.autoinvest.repository.AutoInvestExecutionLedgerRepository;
import com.woorifisa.won_invest_core_server.domain.autoinvest.repository.AutoInvestOrderLedgerRepository;
import com.woorifisa.won_invest_core_server.domain.autoinvest.repository.AutoInvestSweepLedgerRepository;
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
import java.time.LocalDateTime;
import java.util.UUID;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class InternalInvestAutoInvestExecutionApiTest {

    private static final String SERVICE_ID_HEADER = "X-Service-ID";
    private static final String API_KEY_HEADER = "X-Internal-Api-Key";
    private static final String USER_UUID_HEADER = "X-User-UUID";
    private static final String SERVICE_ID = "won-invest-channel";
    private static final String API_KEY = "test-internal-api-key";
    private static final UUID USER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_USER_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID INVEST_USER_UUID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID ACCOUNT_UUID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID CARD_USER_UUID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AutoInvestExecutionLedgerRepository investExecutionLedgerRepository;

    @Autowired
    private AutoInvestOrderLedgerRepository investOrderLedgerRepository;

    @Autowired
    private AutoInvestSweepLedgerRepository autoInvestSweepLedgerRepository;

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
        autoInvestSweepLedgerRepository.deleteAll();
        investEtfProductRepository.deleteAll();
        investAccountRepository.deleteAll();
        investUserRepository.deleteAll();
    }

    @Test
    @DisplayName("정상 조회 시 성공과 실패 이력을 함께 정렬하고 커서를 반환한다")
    void getAutoInvestExecutionHistories_success() throws Exception {
        seedAccount(USER_UUID, AccountStatus.ACTIVE);
        Long vooEtfId = seedEtfProduct("VOO", "Vanguard S&P 500 ETF");
        Long qqqEtfId = seedEtfProduct("QQQ", "Invesco QQQ Trust");

        Long firstCompletedId = seedCompletedHistory(
                1001L,
                vooEtfId,
                "VOO",
                15000L,
                LocalDateTime.of(2026, 6, 10, 22, 0, 0),
                LocalDateTime.of(2026, 6, 10, 22, 0, 3),
                new BigDecimal("0.02730000"),
                new BigDecimal("549.1200"),
                new BigDecimal("14.9730")
        );
        Long failedId = seedFailedHistory(
                1002L,
                vooEtfId,
                12000L,
                LocalDateTime.of(2026, 6, 10, 21, 0, 0),
                AutoInvestFailureCode.PRICE_UNAVAILABLE
        );
        Long secondCompletedId = seedCompletedHistory(
                1003L,
                qqqEtfId,
                "QQQ",
                21000L,
                LocalDateTime.of(2026, 6, 10, 20, 59, 30),
                LocalDateTime.of(2026, 6, 10, 21, 0, 0),
                new BigDecimal("0.03150000"),
                new BigDecimal("666.6600"),
                new BigDecimal("20.9998")
        );

        mockMvc.perform(get("/internal/invest/accounts/{accountUuid}/auto-invest/executions", ACCOUNT_UUID)
                        .header(SERVICE_ID_HEADER, SERVICE_ID)
                        .header(API_KEY_HEADER, API_KEY)
                        .header(USER_UUID_HEADER, USER_UUID.toString())
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("INVEST_200_010"))
                .andExpect(jsonPath("$.message").value("ETF 자동 투자 체결 이력 조회가 완료되었습니다."))
                .andExpect(jsonPath("$.data.histories.length()").value(2))
                .andExpect(jsonPath("$.data.histories[0].sweepExecutionId").value(firstCompletedId))
                .andExpect(jsonPath("$.data.histories[0].ticker").value("VOO"))
                .andExpect(jsonPath("$.data.histories[0].executionStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.histories[0].orderId").isNumber())
                .andExpect(jsonPath("$.data.histories[0].requestedKrwAmount").value(15000.00))
                .andExpect(jsonPath("$.data.histories[0].executedAt").value("2026-06-10T22:00:03+09:00"))
                .andExpect(jsonPath("$.data.histories[1].sweepExecutionId").value(secondCompletedId))
                .andExpect(jsonPath("$.data.histories[1].ticker").value("QQQ"))
                .andExpect(jsonPath("$.data.histories[1].executedAt").value("2026-06-10T21:00:00+09:00"))
                .andExpect(jsonPath("$.data.nextCursor").value("2026-06-10T21:00+09:00|" + secondCompletedId))
                .andExpect(jsonPath("$.data.hasNext").value(true));

        mockMvc.perform(get("/internal/invest/accounts/{accountUuid}/auto-invest/executions", ACCOUNT_UUID)
                        .header(SERVICE_ID_HEADER, SERVICE_ID)
                        .header(API_KEY_HEADER, API_KEY)
                        .header(USER_UUID_HEADER, USER_UUID.toString())
                        .param("cursor", "2026-06-10T21:00:00+09:00|" + secondCompletedId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.histories.length()").value(1))
                .andExpect(jsonPath("$.data.histories[0].sweepExecutionId").value(failedId))
                .andExpect(jsonPath("$.data.histories[0].executionStatus").value("FAILED"))
                .andExpect(jsonPath("$.data.histories[0].failureCode").value("SWEEP_FAIL_006"))
                .andExpect(jsonPath("$.data.histories[0].failureMessage").value("ETF 현재가를 조회할 수 없습니다."))
                .andExpect(jsonPath("$.data.histories[0].requestedAt").value("2026-06-10T21:00:00+09:00"))
                .andExpect(jsonPath("$.data.histories[0].executedAt").value(nullValue()))
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    @DisplayName("ticker는 대문자로 조회하고 status 필터를 적용한다")
    void getAutoInvestExecutionHistories_filters() throws Exception {
        seedAccount(USER_UUID, AccountStatus.ACTIVE);
        Long vooEtfId = seedEtfProduct("VOO", "Vanguard S&P 500 ETF");
        Long qqqEtfId = seedEtfProduct("QQQ", "Invesco QQQ Trust");

        seedCompletedHistory(
                2001L,
                vooEtfId,
                "VOO",
                15000L,
                LocalDateTime.of(2026, 6, 10, 22, 0, 0),
                LocalDateTime.of(2026, 6, 10, 22, 0, 3),
                new BigDecimal("0.02730000"),
                new BigDecimal("549.1200"),
                new BigDecimal("14.9730")
        );
        seedFailedHistory(
                2002L,
                vooEtfId,
                15000L,
                LocalDateTime.of(2026, 6, 10, 21, 0, 0),
                AutoInvestFailureCode.PRICE_UNAVAILABLE
        );
        seedCompletedHistory(
                2003L,
                qqqEtfId,
                "QQQ",
                15000L,
                LocalDateTime.of(2026, 6, 10, 20, 0, 0),
                LocalDateTime.of(2026, 6, 10, 20, 0, 3),
                new BigDecimal("0.01230000"),
                new BigDecimal("500.0000"),
                new BigDecimal("6.1500")
        );

        mockMvc.perform(get("/internal/invest/accounts/{accountUuid}/auto-invest/executions", ACCOUNT_UUID)
                        .header(SERVICE_ID_HEADER, SERVICE_ID)
                        .header(API_KEY_HEADER, API_KEY)
                        .header(USER_UUID_HEADER, USER_UUID.toString())
                        .param("ticker", "voo")
                        .param("status", "completed")
                        .param("from", "2026-06-10T21:30:00+09:00")
                        .param("to", "2026-06-10T22:30:00+09:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.histories.length()").value(1))
                .andExpect(jsonPath("$.data.histories[0].ticker").value("VOO"))
                .andExpect(jsonPath("$.data.histories[0].executionStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.data.histories[0].failureCode").value(nullValue()))
                .andExpect(jsonPath("$.data.hasNext").value(false));
    }

    @Test
    @DisplayName("from이 to보다 늦으면 400을 반환한다")
    void getAutoInvestExecutionHistories_invalidDateRange() throws Exception {
        seedAccount(USER_UUID, AccountStatus.ACTIVE);

        mockMvc.perform(get("/internal/invest/accounts/{accountUuid}/auto-invest/executions", ACCOUNT_UUID)
                        .header(SERVICE_ID_HEADER, SERVICE_ID)
                        .header(API_KEY_HEADER, API_KEY)
                        .header(USER_UUID_HEADER, USER_UUID.toString())
                        .param("from", "2026-06-11T10:00:00+09:00")
                        .param("to", "2026-06-10T10:00:00+09:00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVEST_400_002"))
                .andExpect(jsonPath("$.message").value("자동 투자 체결 이력 조회 조건이 올바르지 않습니다."));
    }

    @Test
    @DisplayName("비활성 계좌면 400을 반환한다")
    void getAutoInvestExecutionHistories_inactiveAccount() throws Exception {
        seedAccount(USER_UUID, AccountStatus.INACTIVE);

        mockMvc.perform(get("/internal/invest/accounts/{accountUuid}/auto-invest/executions", ACCOUNT_UUID)
                        .header(SERVICE_ID_HEADER, SERVICE_ID)
                        .header(API_KEY_HEADER, API_KEY)
                        .header(USER_UUID_HEADER, USER_UUID.toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVEST_400_001"))
                .andExpect(jsonPath("$.message").value("정상 상태의 증권계좌가 아닙니다."));
    }

    @Test
    @DisplayName("본인 계좌가 아니면 403을 반환한다")
    void getAutoInvestExecutionHistories_accessDenied() throws Exception {
        seedAccount(USER_UUID, AccountStatus.ACTIVE);

        mockMvc.perform(get("/internal/invest/accounts/{accountUuid}/auto-invest/executions", ACCOUNT_UUID)
                        .header(SERVICE_ID_HEADER, SERVICE_ID)
                        .header(API_KEY_HEADER, API_KEY)
                        .header(USER_UUID_HEADER, OTHER_USER_UUID.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("INVEST_403_001"));
    }

    @Test
    @DisplayName("size는 최대 100건까지만 반환한다")
    void getAutoInvestExecutionHistories_limitSizeTo100() throws Exception {
        seedAccount(USER_UUID, AccountStatus.ACTIVE);
        Long vooEtfId = seedEtfProduct("VOO", "Vanguard S&P 500 ETF");

        for (int i = 0; i < 101; i++) {
            seedFailedHistory(
                    3000L + i,
                    vooEtfId,
                    10000L + i,
                    LocalDateTime.of(2026, 6, 10, 23, 0, 0).minusMinutes(i),
                    AutoInvestFailureCode.PRICE_UNAVAILABLE
            );
        }

        mockMvc.perform(get("/internal/invest/accounts/{accountUuid}/auto-invest/executions", ACCOUNT_UUID)
                        .header(SERVICE_ID_HEADER, SERVICE_ID)
                        .header(API_KEY_HEADER, API_KEY)
                        .header(USER_UUID_HEADER, USER_UUID.toString())
                        .param("size", "101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.histories.length()").value(100))
                .andExpect(jsonPath("$.data.hasNext").value(true));
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

    private Long seedCompletedHistory(
            Long sweepRequestId,
            Long etfId,
            String ticker,
            Long krwAmount,
            LocalDateTime requestedAt,
            LocalDateTime executedAt,
            BigDecimal quantity,
            BigDecimal executionPrice,
            BigDecimal executionAmount
    ) {
        InvestAccount account = investAccountRepository.findById(ACCOUNT_UUID).orElseThrow();
        InvestEtfProduct etf = investEtfProductRepository.findByExternalProviderAndTicker("KIS", ticker).orElseThrow();
        AutoInvestSweepLedger ledger = autoInvestSweepLedgerRepository.save(AutoInvestSweepLedger.requested(
                newRequest(sweepRequestId, etfId, krwAmount, requestedAt)
        ));

        InvestOrderLedger order = InvestOrderLedger.requestedSweepBuy(
                "idem-" + sweepRequestId,
                sweepRequestId,
                account,
                etf,
                quantity,
                executionAmount,
                executionPrice,
                "USD",
                requestedAt
        );
        order.complete();
        InvestOrderLedger savedOrder = investOrderLedgerRepository.save(order);

        InvestExecutionLedger savedExecution = investExecutionLedgerRepository.save(InvestExecutionLedger.completed(
                savedOrder,
                account,
                etf,
                quantity,
                executionPrice,
                executionAmount,
                executedAt
        ));

        ledger.complete(
                savedOrder,
                savedExecution,
                new BigDecimal("1375.0000"),
                executionPrice,
                quantity,
                BigDecimal.valueOf(krwAmount),
                BigDecimal.ZERO,
                executedAt
        );
        return autoInvestSweepLedgerRepository.save(ledger).getSweepExecutionId();
    }

    private Long seedFailedHistory(
            Long sweepRequestId,
            Long etfId,
            Long krwAmount,
            LocalDateTime requestedAt,
            AutoInvestFailureCode failureCode
    ) {
        AutoInvestSweepLedger ledger = autoInvestSweepLedgerRepository.save(AutoInvestSweepLedger.requested(
                newRequest(sweepRequestId, etfId, krwAmount, requestedAt)
        ));
        ledger.fail(failureCode, requestedAt.plusSeconds(1));
        return autoInvestSweepLedgerRepository.save(ledger).getSweepExecutionId();
    }

    private AutoInvestExecutionRequest newRequest(
            Long sweepRequestId,
            Long etfId,
            Long krwAmount,
            LocalDateTime requestedAt
    ) {
        return new AutoInvestExecutionRequest(
                "event-" + sweepRequestId,
                "SWEEP_REQUESTED",
                "correlation-" + sweepRequestId,
                "idem-" + sweepRequestId,
                sweepRequestId,
                USER_UUID,
                CARD_USER_UUID,
                5000L + sweepRequestId,
                9000L + sweepRequestId,
                "2026-06",
                1000L,
                krwAmount,
                etfId,
                requestedAt
        );
    }
}
