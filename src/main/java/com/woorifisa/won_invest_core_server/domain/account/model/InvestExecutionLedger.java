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
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "invest_execution_ledger")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvestExecutionLedger {

    @Id
    @Column(name = "execution_id", nullable = false)
    private Long executionId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "invest_account_uuid", nullable = false, columnDefinition = "CHAR(36)")
    private UUID investAccountUuid;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "user_uuid", nullable = false, columnDefinition = "CHAR(36)")
    private UUID userUuid;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "invest_user_uuid", nullable = false, columnDefinition = "CHAR(36)")
    private UUID investUserUuid;

    @Column(name = "etf_id", nullable = false)
    private Long etfId;

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

    @Builder
    public InvestExecutionLedger(
            Long executionId,
            Long orderId,
            UUID investAccountUuid,
            UUID userUuid,
            UUID investUserUuid,
            Long etfId,
            String ticker,
            BigDecimal executionQuantity,
            BigDecimal executionPrice,
            BigDecimal executionAmount,
            LocalDateTime executedAt
    ) {
        this.executionId = executionId;
        this.orderId = orderId;
        this.investAccountUuid = investAccountUuid;
        this.userUuid = userUuid;
        this.investUserUuid = investUserUuid;
        this.etfId = etfId;
        this.ticker = ticker;
        this.executionQuantity = executionQuantity;
        this.executionPrice = executionPrice;
        this.executionAmount = executionAmount;
        this.executedAt = executedAt;
    }
}
