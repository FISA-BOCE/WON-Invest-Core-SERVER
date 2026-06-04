package com.woorifisa.won_invest_core_server.domain.account.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "invest_account_etf_holding")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvestAccountEtfHolding {

    @Id
    @Column(name = "etf_holding_id", nullable = false)
    private Long etfHoldingId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "invest_account_uuid", nullable = false, columnDefinition = "CHAR(36)")
    private UUID investAccountUuid;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "invest_user_uuid", nullable = false, columnDefinition = "CHAR(36)")
    private UUID investUserUuid;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "user_uuid", nullable = false, columnDefinition = "CHAR(36)")
    private UUID userUuid;

    @Column(name = "etf_id")
    private Long etfId;

    @Column(name = "ticker", nullable = false, length = 20)
    private String ticker;

    @Column(name = "holding_quantity", nullable = false, precision = 18, scale = 8)
    private BigDecimal holdingQuantity;

    @Column(name = "average_buy_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal averageBuyPrice;

    @Column(name = "total_buy_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal totalBuyAmount;

    @Column(name = "evaluation_amount", precision = 18, scale = 4)
    private BigDecimal evaluationAmount;

    @Column(name = "profit_loss_amount", precision = 18, scale = 4)
    private BigDecimal profitLossAmount;

    @Column(name = "profit_loss_rate", precision = 10, scale = 6)
    private BigDecimal profitLossRate;

    @Builder
    public InvestAccountEtfHolding(
            Long etfHoldingId,
            UUID investAccountUuid,
            UUID investUserUuid,
            UUID userUuid,
            Long etfId,
            String ticker,
            BigDecimal holdingQuantity,
            BigDecimal averageBuyPrice,
            BigDecimal totalBuyAmount,
            BigDecimal evaluationAmount,
            BigDecimal profitLossAmount,
            BigDecimal profitLossRate
    ) {
        this.etfHoldingId = Objects.requireNonNull(etfHoldingId, "etfHoldingId must not be null");
        this.investAccountUuid = investAccountUuid;
        this.investUserUuid = investUserUuid;
        this.userUuid = userUuid;
        this.etfId = etfId;
        this.ticker = ticker;
        this.holdingQuantity = holdingQuantity;
        this.averageBuyPrice = averageBuyPrice;
        this.totalBuyAmount = totalBuyAmount;
        this.evaluationAmount = evaluationAmount;
        this.profitLossAmount = profitLossAmount;
        this.profitLossRate = profitLossRate;
    }
}
