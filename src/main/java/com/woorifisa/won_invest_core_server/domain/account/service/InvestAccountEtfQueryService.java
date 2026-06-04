package com.woorifisa.won_invest_core_server.domain.account.service;

import com.woorifisa.won_invest_core_server.domain.account.dto.response.InvestAccountEtfDetailsResponse;
import com.woorifisa.won_invest_core_server.domain.account.exception.InvestAccountErrorCode;
import com.woorifisa.won_invest_core_server.domain.account.model.AccountStatus;
import com.woorifisa.won_invest_core_server.domain.account.model.InvestAccount;
import com.woorifisa.won_invest_core_server.domain.account.model.InvestAccountEtfHolding;
import com.woorifisa.won_invest_core_server.domain.account.model.InvestOrderType;
import com.woorifisa.won_invest_core_server.domain.account.repository.InvestAccountEtfHoldingRepository;
import com.woorifisa.won_invest_core_server.domain.account.repository.InvestAccountRepository;
import com.woorifisa.won_invest_core_server.domain.account.repository.InvestExecutionLedgerRepository;
import com.woorifisa.won_invest_core_server.domain.account.service.projection.RecentExecutionView;
import com.woorifisa.won_invest_core_server.domain.etf.model.InvestEtfProduct;
import com.woorifisa.won_invest_core_server.domain.etf.repository.InvestEtfProductRepository;
import com.woorifisa.won_invest_core_server.global.exception.handler.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvestAccountEtfQueryService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final InvestAccountRepository investAccountRepository;
    private final InvestAccountEtfHoldingRepository investAccountEtfHoldingRepository;
    private final InvestExecutionLedgerRepository investExecutionLedgerRepository;
    private final InvestEtfProductRepository investEtfProductRepository;

    public InvestAccountEtfDetailsResponse getAccountEtfDetails(UUID accountUuid, UUID userUuid) {
        InvestAccount account = investAccountRepository.findById(accountUuid)
                .orElseThrow(() -> new BusinessException(InvestAccountErrorCode.ACCOUNT_NOT_FOUND));

        if (!account.getUserUuid().equals(userUuid)) {
            throw new BusinessException(InvestAccountErrorCode.ACCOUNT_ACCESS_DENIED);
        }

        if (account.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException(InvestAccountErrorCode.ACCOUNT_NOT_ACTIVE);
        }

        List<InvestAccountEtfHolding> holdings = investAccountEtfHoldingRepository
                .findByInvestAccountUuidAndHoldingQuantityGreaterThanOrderByEtfHoldingIdAsc(accountUuid, ZERO);

        Map<Long, String> etfNameById = investEtfProductRepository.findAllById(holdings.stream()
                        .map(InvestAccountEtfHolding::getEtfId)
                        .filter(id -> id != null)
                        .distinct()
                        .toList())
                .stream()
                .collect(Collectors.toMap(InvestEtfProduct::getEtfId, InvestEtfProduct::getEtfName, (left, right) -> left));

        List<InvestAccountEtfDetailsResponse.HoldingResponse> holdingResponses = holdings.stream()
                .map(holding -> toHoldingResponse(holding, etfNameById))
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
                                List.copyOf(InvestOrderType.buyTypes()),
                                PageRequest.of(0, 3)
                        ).stream()
                        .map(this::toRecentExecutionResponse)
                        .toList();

        return new InvestAccountEtfDetailsResponse(
                totalEvaluationAmount,
                profitLossAmount,
                profitLossRate,
                holdingResponses,
                recentExecutions
        );
    }

    private InvestAccountEtfDetailsResponse.HoldingResponse toHoldingResponse(
            InvestAccountEtfHolding holding,
            Map<Long, String> etfNameById
    ) {
        BigDecimal evaluationAmount = nullSafe(holding.getEvaluationAmount());
        BigDecimal totalBuyAmount = resolveTotalBuyAmount(holding);
        BigDecimal profitLossAmount = holding.getProfitLossAmount() != null
                ? holding.getProfitLossAmount()
                : evaluationAmount.subtract(totalBuyAmount);
        BigDecimal profitLossRate = holding.getProfitLossRate() != null
                ? holding.getProfitLossRate()
                : calculateRate(profitLossAmount, totalBuyAmount);

        return new InvestAccountEtfDetailsResponse.HoldingResponse(
                holding.getEtfId(),
                holding.getEtfId() == null ? null : etfNameById.get(holding.getEtfId()),
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
                view.executedAt(),
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
