package com.woorifisa.won_invest_core_server.domain.account.service;

import com.woorifisa.won_invest_core_server.domain.account.dto.response.InvestAccountEtfDetailsResponse;
import com.woorifisa.won_invest_core_server.domain.account.model.InvestAccount;
import com.woorifisa.won_invest_core_server.domain.account.service.projection.RecentExecutionView;
import com.woorifisa.won_invest_core_server.domain.autoinvest.repository.AutoInvestAccountEtfHoldingRepository;
import com.woorifisa.won_invest_core_server.domain.autoinvest.repository.AutoInvestExecutionLedgerRepository;
import com.woorifisa.won_invest_core_server.domain.autoinvest.model.InvestAccountEtfHolding;
import com.woorifisa.won_invest_core_server.domain.account.exception.InvestAccountErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvestAccountEtfQueryService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final ZoneId KST_ZONE_ID = ZoneId.of("Asia/Seoul");

    private final InvestAccountQuerySupport investAccountQuerySupport;
    private final AutoInvestAccountEtfHoldingRepository investAccountEtfHoldingRepository;
    private final AutoInvestExecutionLedgerRepository investExecutionLedgerRepository;

    public InvestAccountEtfDetailsResponse getAccountEtfDetails(UUID accountUuid, UUID userUuid) {
        InvestAccount account = investAccountQuerySupport.getAccessibleActiveAccount(
                accountUuid,
                userUuid,
                InvestAccountErrorCode.ACCOUNT_NOT_ACTIVE
        );

        List<InvestAccountEtfHolding> holdings = investAccountEtfHoldingRepository
                .findByInvestAccountInvestAccountUuidAndHoldingQuantityGreaterThanOrderByEtfHoldingIdAsc(accountUuid, ZERO);

        List<InvestAccountEtfDetailsResponse.HoldingResponse> holdingResponses = holdings.stream()
                .map(this::toHoldingResponse)
                .toList();

        BigDecimal totalEvaluationAmount = holdings.stream()
                .map(holding -> nullSafe(holding.getEvaluationAmount()))
                .reduce(ZERO, BigDecimal::add);

        BigDecimal totalBuyAmount = holdings.stream()
                .map(this::resolveTotalBuyAmount)
                .reduce(ZERO, BigDecimal::add);

        BigDecimal profitLossAmount = totalEvaluationAmount.subtract(totalBuyAmount);
        BigDecimal profitLossRate = calculateRate(profitLossAmount, totalBuyAmount);

        List<InvestAccountEtfDetailsResponse.RecentExecutionResponse> recentExecutions =
                investExecutionLedgerRepository.findRecentExecutionsByAccountUuid(
                                accountUuid,
                                PageRequest.of(0, 3)
                        ).stream()
                        .map(this::toRecentExecutionResponse)
                        .toList();

        return new InvestAccountEtfDetailsResponse(
                LocalDate.now(KST_ZONE_ID),
                totalEvaluationAmount,
                profitLossAmount,
                profitLossRate,
                holdingResponses,
                recentExecutions
        );
    }

    private InvestAccountEtfDetailsResponse.HoldingResponse toHoldingResponse(InvestAccountEtfHolding holding) {
        BigDecimal evaluationAmount = nullSafe(holding.getEvaluationAmount());
        BigDecimal totalBuyAmount = resolveTotalBuyAmount(holding);
        BigDecimal profitLossAmount = holding.getProfitLossAmount() != null
                ? holding.getProfitLossAmount()
                : evaluationAmount.subtract(totalBuyAmount);
        BigDecimal profitLossRate = calculateRate(profitLossAmount, totalBuyAmount);
        Long etfId = holding.getEtf().getEtfId();

        return new InvestAccountEtfDetailsResponse.HoldingResponse(
                etfId,
                holding.getEtf().getEtfName(),
                holding.getTicker(),
                holding.getHoldingQuantity(),
                holding.getAverageBuyPrice(),
                evaluationAmount,
                profitLossAmount,
                profitLossRate
        );
    }

    private InvestAccountEtfDetailsResponse.RecentExecutionResponse toRecentExecutionResponse(RecentExecutionView view) {
        return new InvestAccountEtfDetailsResponse.RecentExecutionResponse(
                view.executedAt().atZone(KST_ZONE_ID).toOffsetDateTime(),
                view.ticker(),
                view.executionQuantity(),
                view.orderType()
        );
    }

    private BigDecimal resolveTotalBuyAmount(InvestAccountEtfHolding holding) {
        if (holding.getTotalBuyAmount() != null) {
            return holding.getTotalBuyAmount();
        }
        return nullSafe(holding.getAverageBuyPrice()).multiply(nullSafe(holding.getHoldingQuantity()));
    }

    private BigDecimal calculateRate(BigDecimal profitLossAmount, BigDecimal totalBuyAmount) {
        if (totalBuyAmount.compareTo(ZERO) == 0) {
            return ZERO;
        }
        return profitLossAmount
                .multiply(BigDecimal.valueOf(100))
                .divide(totalBuyAmount, 6, RoundingMode.HALF_UP);
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value == null ? ZERO : value;
    }
}
