package com.woorifisa.won_invest_core_server.domain.autoinvest.service;

import com.woorifisa.won_invest_core_server.domain.account.model.enums.AccountStatus;
import com.woorifisa.won_invest_core_server.domain.account.model.InvestAccount;
import com.woorifisa.won_invest_core_server.domain.account.repository.InvestAccountRepository;
import com.woorifisa.won_invest_core_server.domain.autoinvest.dto.request.AutoInvestExecutionRequest;
import com.woorifisa.won_invest_core_server.domain.autoinvest.dto.response.AutoInvestExecutionResponse;
import com.woorifisa.won_invest_core_server.domain.autoinvest.exception.enums.AutoInvestFailureCode;
import com.woorifisa.won_invest_core_server.domain.autoinvest.model.InvestOrderLedger;
import com.woorifisa.won_invest_core_server.domain.autoinvest.model.enums.SweepEventType;
import com.woorifisa.won_invest_core_server.domain.autoinvest.repository.InvestOrderLedgerRepository;
import com.woorifisa.won_invest_core_server.domain.etf.model.InvestEtfProduct;
import com.woorifisa.won_invest_core_server.domain.etf.repository.InvestEtfProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional
@RequiredArgsConstructor
public class AutoInvestExecutionService {

    private static final int QUANTITY_SCALE = 4;
    private static final int MONEY_SCALE = 4;

    private final InvestAccountRepository accountRepository;
    private final InvestEtfProductRepository etfProductRepository;
    private final InvestOrderLedgerRepository orderLedgerRepository;

    // 1. 이벤트 타입이 SWEEP_REQUESTED 인지 확인
    // 2. idempotencyKey로 이미 처리한 주문인지 확인
    // 3. 이미 있으면 기존 결과 반환  > 기존 결과 반환 : 이거 좀 더 구체화야할듯
    // 4. 없으면 executeNew(request) 호출
    public AutoInvestExecutionResponse execute(AutoInvestExecutionRequest request) {
        if (!SweepEventType.SWEEP_REQUESTED.name().equals(request.eventType())) {
            return AutoInvestExecutionResponse.failed(request.idempotencyKey(), AutoInvestFailureCode.INVALID_EVENT_TYPE);
        }

        return orderLedgerRepository.findByIdempotencyKey(request.idempotencyKey())
                .map(order -> AutoInvestExecutionResponse.completed(order.getOrderId(), request.idempotencyKey()))
                .orElseGet(() -> executeNew(request));
    }

    // 중복이 아니라 진짜 처음 온 요청일 때 실행됨
    private AutoInvestExecutionResponse executeNew(AutoInvestExecutionRequest request) {
        InvestAccount account = accountRepository.findByUserUuid(request.userUuid()).orElse(null);

        // 증권 계좌 조회
        if (account == null) {
            return AutoInvestExecutionResponse.failed(request.idempotencyKey(), AutoInvestFailureCode.INVEST_ACCOUNT_NOT_FOUND);
        }

        // 활성 상태인지
        if (!AccountStatus.ACTIVE.equals(account.getAccountStatus())) {
            return AutoInvestExecutionResponse.failed(request.idempotencyKey(), AutoInvestFailureCode.INVEST_ACCOUNT_NOT_ACTIVE);
        }

        InvestEtfProduct etf = etfProductRepository.findById(request.etfId()).orElse(null);

        if (etf == null) {
            // etf error code로 바꾸기
            return AutoInvestExecutionResponse.failed(request.idempotencyKey(), AutoInvestFailureCode.ETF_NOT_FOUND);
        }

        if (!etf.canProvideAutoInvestment()) {
            return AutoInvestExecutionResponse.failed(request.idempotencyKey(), AutoInvestFailureCode.ETF_NOT_AVAILABLE);
        }

        // 리워드 원화 금액으로 매수 수량 계산
        // 여기 수정해야함
        BigDecimal rewardKrw = BigDecimal.valueOf(request.krwAmount());
        BigDecimal fxRate = new BigDecimal("1370.00");
        BigDecimal priceUsd = new BigDecimal("375.40");

        BigDecimal usdBudget = rewardKrw.divide(fxRate, 8, RoundingMode.DOWN);
        BigDecimal quantity = usdBudget.divide(priceUsd, QUANTITY_SCALE, RoundingMode.DOWN);

        BigDecimal orderAmountUsd = quantity.multiply(priceUsd).setScale(MONEY_SCALE, RoundingMode.DOWN);
        BigDecimal usedKrw = orderAmountUsd.multiply(fxRate).setScale(0, RoundingMode.DOWN);
        BigDecimal remainingKrw = rewardKrw.subtract(usedKrw);

        // 변환하고 남은 애는 원화로 저축해주기
        if (remainingKrw.signum() > 0) {
            account.depositKrw(remainingKrw);
        }

        // 주문 원장 저장
        InvestOrderLedger order = InvestOrderLedger.requestedSweepBuy(
                request.idempotencyKey(),
                request.sweepRequestId(),
                account,
                etf,
                quantity,
                orderAmountUsd,
                priceUsd,
                "USD",
                request.requestedAt()
        );

        orderLedgerRepository.save(order);

        // 주문 완료 처리
        order.complete();

        return AutoInvestExecutionResponse.completed(
                order.getOrderId(),
                request.idempotencyKey()
        );

    }

}
