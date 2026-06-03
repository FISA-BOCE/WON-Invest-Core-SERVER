package com.woorifisa.won_invest_core_server.domain.autoinvest.service;

import com.woorifisa.won_invest_core_server.domain.account.model.enums.AccountStatus;
import com.woorifisa.won_invest_core_server.domain.account.model.InvestAccount;
import com.woorifisa.won_invest_core_server.domain.account.repository.InvestAccountRepository;
import com.woorifisa.won_invest_core_server.domain.autoinvest.dto.request.AutoInvestExecutionRequest;
import com.woorifisa.won_invest_core_server.domain.autoinvest.dto.response.AutoInvestExecutionResponse;
import com.woorifisa.won_invest_core_server.domain.autoinvest.exception.enums.AutoInvestFailureCode;
import com.woorifisa.won_invest_core_server.domain.autoinvest.model.*;
import com.woorifisa.won_invest_core_server.domain.autoinvest.model.enums.SweepEventType;
import com.woorifisa.won_invest_core_server.domain.autoinvest.provider.SweepEtfPriceProvider;
import com.woorifisa.won_invest_core_server.domain.autoinvest.provider.SweepFxRateProvider;
import com.woorifisa.won_invest_core_server.domain.autoinvest.repository.*;
import com.woorifisa.won_invest_core_server.domain.etf.model.InvestEtfProduct;
import com.woorifisa.won_invest_core_server.domain.etf.repository.InvestEtfProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
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
    private final AutoInvestRequestLedgerRepository autoInvestRequestLedgerRepository;

    // 1. 이벤트 타입이 SWEEP_REQUESTED 인지 확인
    // 2. idempotencyKey로 이미 처리한 주문인지 확인
    // 3. 이미 있으면 기존 결과 반환  > 기존 결과 반환 : 이거 좀 더 구체화야할듯
    // 4. 없으면 executeNew(request) 호출
    public AutoInvestExecutionResponse execute(AutoInvestExecutionRequest request) {
        return autoInvestRequestLedgerRepository.findByIdempotencyKey(request.idempotencyKey())
                .map(AutoInvestExecutionResponse::from)
                .orElseGet(() -> executeNew(request));
    }

    // 중복이 아니라 진짜 처음 온 요청일 때 실행됨
    private AutoInvestExecutionResponse executeNew(AutoInvestExecutionRequest request) {
        AutoInvestRequestLedger ledger =
                autoInvestRequestLedgerRepository.save(AutoInvestRequestLedger.requested(request));

        if (!SweepEventType.SWEEP_REQUESTED.name().equals(request.eventType())) {
            ledger.fail(AutoInvestFailureCode.INVALID_EVENT_TYPE);
            return AutoInvestExecutionResponse.from(ledger);
        }

        InvestAccount account = accountRepository.findByUserUuid(request.userUuid()).orElse(null);

        if (account == null) {
            ledger.fail(AutoInvestFailureCode.INVEST_ACCOUNT_NOT_FOUND);
            return AutoInvestExecutionResponse.from(ledger);
        }

        if (!AccountStatus.ACTIVE.equals(account.getAccountStatus())) {
            ledger.fail(AutoInvestFailureCode.INVEST_ACCOUNT_NOT_ACTIVE);
            return AutoInvestExecutionResponse.from(ledger);
        }

        InvestEtfProduct etf = etfProductRepository.findById(request.etfId()).orElse(null);

        if (etf == null) {
            ledger.fail(AutoInvestFailureCode.ETF_NOT_FOUND);
            return AutoInvestExecutionResponse.from(ledger);
        }

        if (!etf.canProvideAutoInvestment()) {
            ledger.fail(AutoInvestFailureCode.ETF_NOT_AVAILABLE);
            return AutoInvestExecutionResponse.from(ledger);
        }

        BigDecimal rewardKrw = BigDecimal.valueOf(request.krwAmount());
        BigDecimal fxRate = fxRateProvider.getMonthlySweepUsdKrwRate(request.requestedAt());
        BigDecimal priceUsd = etfPriceProvider.getMonthlySweepEtfExecutionPrice(etf.getTicker(), request.requestedAt());

        BigDecimal usdBudget = rewardKrw.divide(fxRate, 8, RoundingMode.DOWN);
        BigDecimal quantity = usdBudget.divide(priceUsd, QUANTITY_SCALE, RoundingMode.DOWN);

        if (quantity.signum() <= 0) {
            account.depositKrw(rewardKrw);
            ledger.fail(AutoInvestFailureCode.INSUFFICIENT_AMOUNT);
            return AutoInvestExecutionResponse.from(ledger);
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

        InvestExecutionLedger execution = InvestExecutionLedger.completed(
                order,
                account,
                etf,
                quantity,
                priceUsd,
                orderAmountUsd,
                LocalDateTime.now()
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
                remainingKrw
        );

        return AutoInvestExecutionResponse.from(ledger);
    }

}
