package com.woorifisa.won_invest_core_server.domain.autoinvest.service;

import com.woorifisa.won_invest_core_server.domain.account.model.InvestAccount;
import com.woorifisa.won_invest_core_server.domain.account.model.InvestUser;
import com.woorifisa.won_invest_core_server.domain.account.model.enums.AccountStatus;
import com.woorifisa.won_invest_core_server.domain.account.repository.InvestAccountRepository;
import com.woorifisa.won_invest_core_server.domain.autoinvest.dto.request.AutoInvestExecutionRequest;
import com.woorifisa.won_invest_core_server.domain.autoinvest.dto.response.AutoInvestExecutionResponse;
import com.woorifisa.won_invest_core_server.domain.autoinvest.model.InvestAccountEtfHolding;
import com.woorifisa.won_invest_core_server.domain.autoinvest.model.InvestAccountEtfLedger;
import com.woorifisa.won_invest_core_server.domain.autoinvest.model.InvestExecutionLedger;
import com.woorifisa.won_invest_core_server.domain.autoinvest.model.InvestOrderLedger;
import com.woorifisa.won_invest_core_server.domain.autoinvest.model.enums.AutoInvestExecutionStatus;
import com.woorifisa.won_invest_core_server.domain.autoinvest.model.enums.OrderStatus;
import com.woorifisa.won_invest_core_server.domain.autoinvest.provider.SweepEtfPriceProvider;
import com.woorifisa.won_invest_core_server.domain.autoinvest.provider.SweepFxRateProvider;
import com.woorifisa.won_invest_core_server.domain.autoinvest.repository.InvestAccountEtfHoldingRepository;
import com.woorifisa.won_invest_core_server.domain.autoinvest.repository.InvestAccountEtfLedgerRepository;
import com.woorifisa.won_invest_core_server.domain.autoinvest.repository.InvestExecutionLedgerRepository;
import com.woorifisa.won_invest_core_server.domain.autoinvest.repository.InvestOrderLedgerRepository;
import com.woorifisa.won_invest_core_server.domain.etf.model.InvestEtfProduct;
import com.woorifisa.won_invest_core_server.domain.etf.model.enums.EtfCurrency;
import com.woorifisa.won_invest_core_server.domain.etf.model.enums.EtfProductStatus;
import com.woorifisa.won_invest_core_server.domain.etf.repository.InvestEtfProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutoInvestExecutionServiceTest {

    @Mock
    private InvestAccountRepository accountRepository;

    @Mock
    private InvestEtfProductRepository etfProductRepository;

    @Mock
    private InvestOrderLedgerRepository orderLedgerRepository;

    @Mock
    private InvestExecutionLedgerRepository executionLedgerRepository;

    @Mock
    private InvestAccountEtfHoldingRepository holdingRepository;

    @Mock
    private InvestAccountEtfLedgerRepository etfLedgerRepository;

    @Mock
    private SweepFxRateProvider fxRateProvider;

    @Mock
    private SweepEtfPriceProvider etfPriceProvider;

    @InjectMocks
    private AutoInvestExecutionService service;

    private static final UUID USER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID CARD_USER_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID INVEST_USER_UUID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID INVEST_ACCOUNT_UUID = UUID.fromString("44444444-4444-4444-4444-444444444444");

    @Test
    @DisplayName("정상 요청이면 자동투자 주문 원장을 저장하고 완료 응답을 반환한다")
    void executeSuccessCreatesOrderLedger() {
        // given
        AutoInvestExecutionRequest request = validRequest();

        InvestAccount account = activeAccount();
        InvestEtfProduct etf = activeEtf();

        given(orderLedgerRepository.findByIdempotencyKey(request.idempotencyKey()))
                .willReturn(Optional.empty());
        given(accountRepository.findByUserUuid(USER_UUID))
                .willReturn(Optional.of(account));
        given(etfProductRepository.findById(request.etfId()))
                .willReturn(Optional.of(etf));
        given(fxRateProvider.getMonthlySweepUsdKrwRate(request.requestedAt()))
                .willReturn(new BigDecimal("1370.00"));
        given(etfPriceProvider.getMonthlySweepEtfExecutionPrice("VOO", request.requestedAt()))
                .willReturn(new BigDecimal("375.40"));
        given(orderLedgerRepository.save(any(InvestOrderLedger.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(executionLedgerRepository.save(any(InvestExecutionLedger.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(holdingRepository.findByInvestAccountInvestAccountUuidAndEtfEtfId(
                account.getInvestAccountUuid(),
                etf.getEtfId()
        )).willReturn(Optional.empty());
        given(holdingRepository.save(any(InvestAccountEtfHolding.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(etfLedgerRepository.save(any(InvestAccountEtfLedger.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        AutoInvestExecutionResponse response = service.execute(request);

        // then
        assertThat(response.status()).isEqualTo(AutoInvestExecutionStatus.COMPLETED);
        assertThat(response.idempotencyKey()).isEqualTo(request.idempotencyKey());

        ArgumentCaptor<InvestOrderLedger> captor = ArgumentCaptor.forClass(InvestOrderLedger.class);
        verify(orderLedgerRepository).save(captor.capture());

        InvestOrderLedger savedOrder = captor.getValue();
        assertThat(savedOrder.getIdempotencyKey()).isEqualTo(request.idempotencyKey());
        assertThat(savedOrder.getSweepId()).isEqualTo(request.sweepRequestId());
        assertThat(savedOrder.getTicker()).isEqualTo("VOO");
        assertThat(savedOrder.getOrderCurrency()).isEqualTo("USD");
        assertThat(savedOrder.getOrderQuantity()).isEqualByComparingTo("0.0194");
        assertThat(savedOrder.getOrderPriceSnapshot()).isEqualByComparingTo("375.40");
        assertThat(savedOrder.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED);

        assertThat(account.getKrwBalanceAmount()).isGreaterThan(BigDecimal.ZERO);

        ArgumentCaptor<InvestExecutionLedger> executionCaptor = ArgumentCaptor.forClass(InvestExecutionLedger.class);
        verify(executionLedgerRepository).save(executionCaptor.capture());
        InvestExecutionLedger savedExecution = executionCaptor.getValue();
        assertThat(savedExecution.getTicker()).isEqualTo("VOO");
        assertThat(savedExecution.getExecutionQuantity()).isEqualByComparingTo("0.0194");
        assertThat(savedExecution.getExecutionPrice()).isEqualByComparingTo("375.40");
        assertThat(savedExecution.getExecutionAmount()).isEqualByComparingTo("7.2827");

        ArgumentCaptor<InvestAccountEtfHolding> holdingCaptor = ArgumentCaptor.forClass(InvestAccountEtfHolding.class);
        verify(holdingRepository).save(holdingCaptor.capture());
        InvestAccountEtfHolding savedHolding = holdingCaptor.getValue();
        assertThat(savedHolding.getTicker()).isEqualTo("VOO");
        assertThat(savedHolding.getHoldingQuantity()).isEqualByComparingTo("0.0194");
        assertThat(savedHolding.getAverageBuyPrice()).isEqualByComparingTo("375.3969");
        assertThat(savedHolding.getTotalBuyAmount()).isEqualByComparingTo("7.2827");

        verify(etfLedgerRepository).save(any(InvestAccountEtfLedger.class));
    }

    @Test
    @DisplayName("같은 idempotencyKey로 이미 주문이 있으면 새 주문을 저장하지 않는다")
    void executeDuplicateReturnsExistingOrder() {
        // given
        AutoInvestExecutionRequest request = validRequest();
        InvestOrderLedger existingOrder = InvestOrderLedger.requestedSweepBuy(
                request.idempotencyKey(),
                request.sweepRequestId(),
                activeAccount(),
                activeEtf(),
                new BigDecimal("0.0194"),
                new BigDecimal("7.2815"),
                new BigDecimal("375.40"),
                "USD",
                request.requestedAt()
        );
        existingOrder.complete();

        given(orderLedgerRepository.findByIdempotencyKey(request.idempotencyKey()))
                .willReturn(Optional.of(existingOrder));
        given(executionLedgerRepository.findByOrderOrderId(existingOrder.getOrderId()))
                .willReturn(Optional.empty());

        // when
        AutoInvestExecutionResponse response = service.execute(request);

        // then
        assertThat(response.status()).isEqualTo(AutoInvestExecutionStatus.COMPLETED);
        verify(orderLedgerRepository, never()).save(any());
        verify(accountRepository, never()).findByUserUuid(any());
        verify(etfProductRepository, never()).findById(any());
        verify(executionLedgerRepository).findByOrderOrderId(existingOrder.getOrderId());
        verify(holdingRepository, never()).save(any());
        verify(etfLedgerRepository, never()).save(any());
    }

    @Test
    @DisplayName("증권 계좌가 없으면 실패 응답을 반환한다")
    void executeAccountNotFoundReturnsFailed() {
        // given
        AutoInvestExecutionRequest request = validRequest();

        given(orderLedgerRepository.findByIdempotencyKey(request.idempotencyKey()))
                .willReturn(Optional.empty());
        given(accountRepository.findByUserUuid(USER_UUID))
                .willReturn(Optional.empty());

        // when
        AutoInvestExecutionResponse response = service.execute(request);

        // then
        assertThat(response.status()).isEqualTo(AutoInvestExecutionStatus.FAILED);
        assertThat(response.failureCode()).isEqualTo("SWEEP_FAIL_002");
        verify(orderLedgerRepository, never()).save(any());
        verify(executionLedgerRepository, never()).save(any());
        verify(holdingRepository, never()).save(any());
        verify(etfLedgerRepository, never()).save(any());
    }

    @Test
    @DisplayName("ETF가 자동투자 불가능하면 실패 응답을 반환한다")
    void executeUnavailableEtfReturnsFailed() {
        // given
        AutoInvestExecutionRequest request = validRequest();

        given(orderLedgerRepository.findByIdempotencyKey(request.idempotencyKey()))
                .willReturn(Optional.empty());
        given(accountRepository.findByUserUuid(USER_UUID))
                .willReturn(Optional.of(activeAccount()));
        given(etfProductRepository.findById(request.etfId()))
                .willReturn(Optional.of(inactiveEtf()));

        // when
        AutoInvestExecutionResponse response = service.execute(request);

        // then
        assertThat(response.status()).isEqualTo(AutoInvestExecutionStatus.FAILED);
        assertThat(response.failureCode()).isEqualTo("SWEEP_FAIL_005");
        verify(orderLedgerRepository, never()).save(any());
        verify(executionLedgerRepository, never()).save(any());
        verify(holdingRepository, never()).save(any());
        verify(etfLedgerRepository, never()).save(any());
    }

    private AutoInvestExecutionRequest validRequest() {
        return new AutoInvestExecutionRequest(
                "CARD-SWEEP-TEST-1",
                "SWEEP_REQUESTED",
                "corr-test-1",
                "sweep-test-key-1",
                1L,
                USER_UUID,
                CARD_USER_UUID,
                1L,
                1L,
                "2026-06",
                10_000L,
                10_000L,
                1L,
                LocalDateTime.of(2026, 6, 16, 0, 30)
        );
    }

    private InvestAccount activeAccount() {
        InvestUser user = InvestUser.builder()
                .investUserUuid(INVEST_USER_UUID)
                .userUuid(USER_UUID)
                .customerName("홍길동")
                .telEnc("enc_tel")
                .emailEnc("enc_email")
                .build();

        return InvestAccount.builder()
                .investAccountUuid(INVEST_ACCOUNT_UUID)
                .investUser(user)
                .userUuid(USER_UUID)
                .accountPasswordEnc("enc_password")
                .accountNo("123456789012")
                .accountStatus(AccountStatus.ACTIVE)
                .openedAt(LocalDateTime.now())
                .build();
    }

    private InvestEtfProduct activeEtf() {
        return InvestEtfProduct.builder()
                .etfId(1L)
                .externalProvider("KIS")
                .externalEtfId("VOO")
                .ticker("VOO")
                .isin("US9229083632")
                .etfName("Vanguard S&P 500 ETF")
                .market("NYSE")
                .currency(EtfCurrency.USD)
                .productStatus(EtfProductStatus.ACTIVE)
                .isFractionalAvailable(true)
                .isTradeAvailable(true)
                .lastSyncedAt(LocalDateTime.now())
                .build();
    }

    private InvestEtfProduct inactiveEtf() {
        return InvestEtfProduct.builder()
                .etfId(1L)
                .externalProvider("KIS")
                .externalEtfId("VOO")
                .ticker("VOO")
                .isin("US9229083632")
                .etfName("Vanguard S&P 500 ETF")
                .market("NYSE")
                .currency(EtfCurrency.USD)
                .productStatus(EtfProductStatus.INACTIVE)
                .isFractionalAvailable(true)
                .isTradeAvailable(true)
                .lastSyncedAt(LocalDateTime.now())
                .build();
    }
}
