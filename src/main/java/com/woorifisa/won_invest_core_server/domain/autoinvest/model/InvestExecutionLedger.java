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
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Entity
@Table(name = "invest_execution_ledger")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvestExecutionLedger extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "execution_id")
    private Long executionId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private InvestOrderLedger order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "invest_account_uuid", nullable = false)
    private InvestAccount investAccount;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "user_uuid", nullable = false, columnDefinition = "CHAR(36)")
    private UUID userUuid;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "invest_user_uuid", nullable = false, columnDefinition = "CHAR(36)")
    private UUID investUserUuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etf_id", nullable = false)
    private InvestEtfProduct etf;

    @Column(name = "ticker", nullable = false, length = 20)
    private String ticker;

    @Column(name = "execution_quantity", nullable = false, precision = 18, scale = 8)
    private BigDecimal executionQuantity;

    @Column(name = "execution_price", nullable = false, precision = 18, scale = 4)
    private BigDecimal executionPrice;

    @Column(name = "execution_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal executionAmount;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;

    public static InvestExecutionLedger completed(
            InvestOrderLedger order,
            InvestAccount account,
            InvestEtfProduct etf,
            BigDecimal quantity,
            BigDecimal executionPrice,
            BigDecimal executionAmount,
            LocalDateTime executedAt
    ) {
        InvestExecutionLedger execution = new InvestExecutionLedger();
        execution.order = order;
        execution.investAccount = account;
        execution.userUuid = account.getUserUuid();
        execution.investUserUuid = account.getInvestUser().getInvestUserUuid();
        execution.etf = etf;
        execution.ticker = etf.getTicker();
        execution.executionQuantity = quantity;
        execution.executionPrice = executionPrice;
        execution.executionAmount = executionAmount;
        execution.executedAt = executedAt != null ? executedAt : LocalDateTime.now();
        return execution;
    }
}
