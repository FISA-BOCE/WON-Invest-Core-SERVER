package com.woorifisa.won_invest_core_server.domain.autoinvest.model;

import com.woorifisa.won_invest_core_server.domain.autoinvest.dto.request.AutoInvestExecutionRequest;
import com.woorifisa.won_invest_core_server.domain.autoinvest.exception.enums.AutoInvestFailureCode;
import com.woorifisa.won_invest_core_server.domain.autoinvest.model.enums.AutoInvestExecutionStatus;
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
@Table(
        name = "auto_invest_sweep_ledger",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_auto_invest_sweep_ledger_idempotency_key", columnNames = "idempotency_key"),
                @UniqueConstraint(name = "uk_auto_invest_sweep_ledger_sweep_request_id", columnNames = "sweep_request_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AutoInvestSweepLedger extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sweep_execution_id")
    private Long sweepExecutionId;

    @Column(name = "sweep_request_id", nullable = false)
    private Long sweepRequestId;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @Column(name = "correlation_id", nullable = false, length = 100)
    private String correlationId;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "user_uuid", nullable = false, columnDefinition = "CHAR(36)")
    private UUID userUuid;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "card_user_uuid", nullable = false, columnDefinition = "CHAR(36)")
    private UUID cardUserUuid;

    @Column(name = "performance_id", nullable = false)
    private Long performanceId;

    @Column(name = "point_ledger_id", nullable = false)
    private Long pointLedgerId;

    @Column(name = "base_month", nullable = false, length = 7)
    private String baseMonth;

    @Column(name = "point_amount", nullable = false)
    private Long pointAmount;

    @Column(name = "krw_amount", nullable = false)
    private Long krwAmount;

    @Column(name = "etf_id", nullable = false)
    private Long etfId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private AutoInvestExecutionStatus status;

    @Column(name = "failure_code", length = 50)
    private String failureCode;

    @Column(name = "failure_message", length = 500)
    private String failureMessage;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "execution_ledger_id")
    private Long executionLedgerId;

    @Column(name = "fx_rate_snapshot", precision = 18, scale = 4)
    private BigDecimal fxRateSnapshot;

    @Column(name = "etf_price_snapshot", precision = 18, scale = 4)
    private BigDecimal etfPriceSnapshot;

    @Column(name = "order_quantity", precision = 18, scale = 8)
    private BigDecimal orderQuantity;

    @Column(name = "used_krw_amount", precision = 18, scale = 4)
    private BigDecimal usedKrwAmount;

    @Column(name = "refund_krw_amount", precision = 18, scale = 4)
    private BigDecimal refundKrwAmount;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public static AutoInvestSweepLedger requested(AutoInvestExecutionRequest request) {
        AutoInvestSweepLedger ledger = new AutoInvestSweepLedger();
        ledger.sweepRequestId = request.sweepRequestId();
        ledger.idempotencyKey = request.idempotencyKey();
        ledger.correlationId = request.correlationId();
        ledger.userUuid = request.userUuid();
        ledger.cardUserUuid = request.cardUserUuid();
        ledger.performanceId = request.performanceId();
        ledger.pointLedgerId = request.pointLedgerId();
        ledger.baseMonth = request.baseMonth();
        ledger.pointAmount = request.pointAmount();
        ledger.krwAmount = request.krwAmount();
        ledger.etfId = request.etfId();
        ledger.status = AutoInvestExecutionStatus.REQUESTED;
        ledger.requestedAt = request.requestedAt();
        return ledger;
    }

    public void fail(AutoInvestFailureCode failureCode) {
        this.status = AutoInvestExecutionStatus.FAILED;
        this.failureCode = failureCode.getCode();
        this.failureMessage = failureCode.getMessage();
        this.completedAt = LocalDateTime.now();
    }

    public void complete(
            InvestOrderLedger order,
            InvestExecutionLedger execution,
            BigDecimal fxRateSnapshot,
            BigDecimal etfPriceSnapshot,
            BigDecimal orderQuantity,
            BigDecimal usedKrwAmount,
            BigDecimal refundKrwAmount
    ) {
        this.status = AutoInvestExecutionStatus.COMPLETED;
        this.failureCode = null;
        this.failureMessage = null;
        this.orderId = order.getOrderId();
        this.executionLedgerId = execution.getExecutionId();
        this.fxRateSnapshot = fxRateSnapshot;
        this.etfPriceSnapshot = etfPriceSnapshot;
        this.orderQuantity = orderQuantity;
        this.usedKrwAmount = usedKrwAmount;
        this.refundKrwAmount = refundKrwAmount;
        this.completedAt = LocalDateTime.now();
    }
}
