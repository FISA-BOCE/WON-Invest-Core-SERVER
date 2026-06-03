package com.woorifisa.won_invest_core_server.domain.autoinvest.service;

import com.woorifisa.won_invest_core_server.domain.account.model.enums.AccountStatus;
import com.woorifisa.won_invest_core_server.domain.account.model.InvestAccount;
import com.woorifisa.won_invest_core_server.domain.account.repository.InvestAccountRepository;
import com.woorifisa.won_invest_core_server.domain.autoinvest.dto.request.AutoInvestExecutionRequest;
import com.woorifisa.won_invest_core_server.domain.autoinvest.dto.response.AutoInvestExecutionResponse;
import com.woorifisa.won_invest_core_server.domain.autoinvest.exception.code.AutoInvestFailureCode;
import com.woorifisa.won_invest_core_server.domain.autoinvest.model.*;
import com.woorifisa.won_invest_core_server.domain.autoinvest.model.enums.SweepEventType;
import com.woorifisa.won_invest_core_server.domain.autoinvest.provider.SweepEtfPriceProvider;
import com.woorifisa.won_invest_core_server.domain.autoinvest.provider.SweepFxRateProvider;
import com.woorifisa.won_invest_core_server.domain.autoinvest.repository.*;
import com.woorifisa.won_invest_core_server.domain.etf.model.InvestEtfProduct;
import com.woorifisa.won_invest_core_server.domain.etf.repository.InvestEtfProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AutoInvestExecutionService {

    private static final int QUANTITY_SCALE = 4;
    private static final int MONEY_SCALE = 4;
    private static final String ORDER_CURRENCY_USD = "USD";

    private final InvestAccountRepository accountRepository;
    private final InvestEtfProductRepository etfProductRepository;
    private final InvestOrderLedgerRepository orderLedgerRepository;
    private final InvestExecutionLedgerRepository executionLedgerRepository;
    private final InvestAccountEtfHoldingRepository holdingRepository;
    private final InvestAccountEtfLedgerRepository etfLedgerRepository;
    private final SweepFxRateProvider fxRateProvider;
    private final SweepEtfPriceProvider etfPriceProvider;
    private final AutoInvestSweepLedgerRepository autoInvestSweepLedgerRepository;

    // 1. 이벤트 타입이 SWEEP_REQUESTED 인지 확인
    // 2. idempotencyKey로 이미 처리한 주문인지 확인
    // 3. 이미 있으면 기존 결과 반환
    // 4. 없으면 executeNew(request) 호출
    public AutoInvestExecutionResponse execute(AutoInvestExecutionRequest request) {
        log.info(
                "자동투자 스윕 실행 요청 수신. correlationId={}, idempotencyKey={}, sweepRequestId={}, pointLedgerId={}, etfId={}",
                request.correlationId(),
                request.idempotencyKey(),
                request.sweepRequestId(),
                request.pointLedgerId(),
                request.etfId()
        );

        return autoInvestSweepLedgerRepository.findByIdempotencyKey(request.idempotencyKey())
                .map(ledger -> {
                    log.info(
                            "자동투자 스윕 중복 요청으로 기존 결과를 반환합니다. correlationId={}, originalCorrelationId={}, idempotencyKey={}, sweepExecutionId={}, status={}",
                            request.correlationId(),
                            ledger.getCorrelationId(),
                            request.idempotencyKey(),
                            ledger.getSweepExecutionId(),
                            ledger.getStatus()
                    );
                    return AutoInvestExecutionResponse.from(ledger);
                })
                .orElseGet(() -> executeNew(request));
    }

    private AutoInvestExecutionResponse executeNew(AutoInvestExecutionRequest request) {
        AutoInvestSweepLedger ledger;

        try {
            ledger = autoInvestSweepLedgerRepository.save(AutoInvestSweepLedger.requested(request));
        } catch (DataIntegrityViolationException exception) {
            return returnExistingLedgerAfterConcurrentDuplicate(request, exception);
        }

        if (!SweepEventType.SWEEP_REQUESTED.name().equals(request.eventType())) {
            return fail(ledger, request, AutoInvestFailureCode.INVALID_EVENT_TYPE);
        }

        InvestAccount account = accountRepository.findByUserUuid(request.userUuid()).orElse(null);

        if (account == null) {
            return fail(ledger, request, AutoInvestFailureCode.INVEST_ACCOUNT_NOT_FOUND);
        }

        if (!AccountStatus.ACTIVE.equals(account.getAccountStatus())) {
            return fail(ledger, request, AutoInvestFailureCode.INVEST_ACCOUNT_NOT_ACTIVE);
        }

        InvestEtfProduct etf = etfProductRepository.findById(request.etfId()).orElse(null);

        if (etf == null) {
            return fail(ledger, request, AutoInvestFailureCode.ETF_NOT_FOUND);
        }

        if (!etf.canProvideAutoInvestment()) {
            return fail(ledger, request, AutoInvestFailureCode.ETF_NOT_AVAILABLE);
        }

        BigDecimal rewardKrw = BigDecimal.valueOf(request.krwAmount());
        BigDecimal fxRate = fxRateProvider.getMonthlySweepUsdKrwRate(request.requestedAt());
        BigDecimal priceUsd = etfPriceProvider.getMonthlySweepEtfExecutionPrice(etf.getTicker(), request.requestedAt());

        if (isNullOrNotPositive(fxRate)) {
            return fail(ledger, request, AutoInvestFailureCode.FX_RATE_UNAVAILABLE);
        }

        if (isNullOrNotPositive(priceUsd)) {
            return fail(ledger, request, AutoInvestFailureCode.PRICE_UNAVAILABLE);
        }

        BigDecimal usdBudget = rewardKrw.divide(fxRate, 8, RoundingMode.DOWN);
        BigDecimal quantity = usdBudget.divide(priceUsd, QUANTITY_SCALE, RoundingMode.DOWN);

        if (quantity.signum() <= 0) {
            account.depositKrw(rewardKrw);
            return fail(ledger, request, AutoInvestFailureCode.INSUFFICIENT_AMOUNT);
        }

        BigDecimal orderAmountUsd = quantity.multiply(priceUsd).setScale(MONEY_SCALE, RoundingMode.DOWN);
        BigDecimal usedKrw = orderAmountUsd.multiply(fxRate).setScale(0, RoundingMode.DOWN);
        BigDecimal remainingKrw = rewardKrw.subtract(usedKrw);

        if (remainingKrw.signum() > 0) {
            account.depositKrw(remainingKrw);
        }

        InvestOrderLedger order = InvestOrderLedger.requestedSweepBuy(
                request.idempotencyKey(),
                request.sweepRequestId(),
                account,
                etf,
                quantity,
                orderAmountUsd,
                priceUsd,
                ORDER_CURRENCY_USD,
                request.requestedAt()
        );

        orderLedgerRepository.save(order);

        LocalDateTime processedAt = LocalDateTime.now();

        InvestExecutionLedger execution = InvestExecutionLedger.completed(
                order,
                account,
                etf,
                quantity,
                priceUsd,
                orderAmountUsd,
                processedAt
        );
        executionLedgerRepository.save(execution);

        InvestAccountEtfHolding holding = holdingRepository
                .findByInvestAccountInvestAccountUuidAndEtfEtfId(
                        account.getInvestAccountUuid(),
                        etf.getEtfId()
                )
                .orElseGet(() -> InvestAccountEtfHolding.empty(account, etf));

        holding.buy(quantity, priceUsd, orderAmountUsd);
        holdingRepository.save(holding);

        etfLedgerRepository.save(InvestAccountEtfLedger.buy(account, etf));

        order.complete();

        ledger.complete(
                order,
                execution,
                fxRate,
                priceUsd,
                quantity,
                usedKrw,
                remainingKrw,
                processedAt
        );

        log.info(
                "자동투자 스윕 실행 완료. correlationId={}, idempotencyKey={}, sweepExecutionId={}, orderId={}, executionLedgerId={}, quantity={}, usedKrw={}, refundKrw={}",
                request.correlationId(),
                request.idempotencyKey(),
                ledger.getSweepExecutionId(),
                order.getOrderId(),
                execution.getExecutionId(),
                quantity,
                usedKrw,
                remainingKrw
        );

        return AutoInvestExecutionResponse.from(ledger);
    }

    private boolean isNullOrNotPositive(BigDecimal value) {
        return value == null || value.signum() <= 0;
    }

    private AutoInvestExecutionResponse returnExistingLedgerAfterConcurrentDuplicate(
            AutoInvestExecutionRequest request,
            DataIntegrityViolationException exception
    ) {
        AutoInvestSweepLedger ledger = autoInvestSweepLedgerRepository.findByIdempotencyKey(request.idempotencyKey())
                .orElseThrow(() -> exception);

        log.info(
                "자동투자 스윕 동시 중복 요청으로 기존 결과를 반환합니다. correlationId={}, originalCorrelationId={}, idempotencyKey={}, sweepExecutionId={}, status={}",
                request.correlationId(),
                ledger.getCorrelationId(),
                request.idempotencyKey(),
                ledger.getSweepExecutionId(),
                ledger.getStatus()
        );

        return AutoInvestExecutionResponse.from(ledger);
    }

    private AutoInvestExecutionResponse fail(
            AutoInvestSweepLedger ledger,
            AutoInvestExecutionRequest request,
            AutoInvestFailureCode failureCode
    ) {
        LocalDateTime failedAt = LocalDateTime.now();
        ledger.fail(failureCode, failedAt);

        log.warn(
                "자동투자 스윕 실행 실패. correlationId={}, idempotencyKey={}, sweepExecutionId={}, sweepRequestId={}, failureCode={}, failureMessage={}",
                request.correlationId(),
                request.idempotencyKey(),
                ledger.getSweepExecutionId(),
                request.sweepRequestId(),
                failureCode.getCode(),
                failureCode.getMessage()
        );

        return AutoInvestExecutionResponse.from(ledger);
    }

}
