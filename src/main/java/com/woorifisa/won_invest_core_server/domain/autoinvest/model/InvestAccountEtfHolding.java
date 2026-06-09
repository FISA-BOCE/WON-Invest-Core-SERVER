package com.woorifisa.won_invest_core_server.domain.autoinvest.model;

import com.woorifisa.won_invest_core_server.domain.account.model.InvestAccount;
import com.woorifisa.won_invest_core_server.domain.etf.model.InvestEtfProduct;
import com.woorifisa.won_invest_core_server.global.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Getter
@Entity(name = "AutoInvestAccountEtfHolding")
@Table(
        name = "invest_account_etf_holding",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_invest_account_etf_holding_account_etf",
                columnNames = {"invest_account_uuid", "etf_id"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvestAccountEtfHolding extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "etf_holding_id")
    private Long etfHoldingId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invest_account_uuid", nullable = false)
    private InvestAccount investAccount;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "invest_user_uuid", nullable = false, columnDefinition = "CHAR(36)")
    private UUID investUserUuid;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "user_uuid", nullable = false, columnDefinition = "CHAR(36)")
    private UUID userUuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etf_id", nullable = false)
    private InvestEtfProduct etf;

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

    // 이건 처음으로 어떤 ETF를 사는 사용자에게 빈 보유 잔고 row 생성
    public static InvestAccountEtfHolding empty(InvestAccount account, InvestEtfProduct etf) {
        InvestAccountEtfHolding holding = new InvestAccountEtfHolding();
        holding.investAccount = account;
        holding.investUserUuid = account.getInvestUser().getInvestUserUuid();
        holding.userUuid = account.getUserUuid();
        holding.etf = etf;
        holding.ticker = etf.getTicker();
        holding.holdingQuantity = BigDecimal.ZERO;
        holding.averageBuyPrice = BigDecimal.ZERO;
        holding.totalBuyAmount = BigDecimal.ZERO;
        holding.evaluationAmount = BigDecimal.ZERO;
        holding.profitLossAmount = BigDecimal.ZERO;
        holding.profitLossRate = BigDecimal.ZERO;
        return holding;
    }

    //  ETF 매수가 체결됐을 때 보유 잔고를 갱신
    public void buy(BigDecimal quantity, BigDecimal executionPrice, BigDecimal executionAmount) {
        BigDecimal currentBuyPriceAmount = this.averageBuyPrice.multiply(this.holdingQuantity);
        BigDecimal newBuyPriceAmount = executionPrice.multiply(quantity);
        BigDecimal newQuantity = this.holdingQuantity.add(quantity);
        BigDecimal newTotalAmount = this.totalBuyAmount.add(executionAmount);

        this.holdingQuantity = newQuantity;
        this.totalBuyAmount = newTotalAmount;
        this.averageBuyPrice = currentBuyPriceAmount
                .add(newBuyPriceAmount)
                .divide(newQuantity, 4, RoundingMode.HALF_UP);

        updateValuation(executionPrice);
    }

    // 현재 가격 기준으로 평가금액과 손익을 다시 계산하는 메서드
    public void updateValuation(BigDecimal currentPrice) {
        this.evaluationAmount = this.holdingQuantity
                .multiply(currentPrice)
                .setScale(4, RoundingMode.HALF_UP);

        this.profitLossAmount = this.evaluationAmount.subtract(this.totalBuyAmount);

        if (this.totalBuyAmount.signum() == 0) {
            this.profitLossRate = BigDecimal.ZERO;
            return;
        }

        this.profitLossRate = this.profitLossAmount
                .divide(this.totalBuyAmount, 6, RoundingMode.HALF_UP);
    }

}
