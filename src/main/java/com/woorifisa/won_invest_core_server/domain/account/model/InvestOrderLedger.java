package com.woorifisa.won_invest_core_server.domain.account.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "invest_order_ledger")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvestOrderLedger {

    @Id
    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "sweep_request_id")
    private Long sweepRequestId;

    @Column(name = "fx_ledger_id")
    private Long fxLedgerId;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 30)
    private InvestOrderType orderType;

    @Column(name = "order_method", nullable = false, length = 30)
    private String orderMethod;

    @Column(name = "order_currency", nullable = false, length = 10)
    private String orderCurrency;

    @Column(name = "order_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal orderAmount;

    @Column(name = "order_quantity", nullable = false, precision = 18, scale = 8)
    private BigDecimal orderQuantity;

    @Column(name = "reference_price", precision = 18, scale = 4)
    private BigDecimal referencePrice;

    @Column(name = "order_status", nullable = false, length = 30)
    private String orderStatus;

    @Column(name = "external_order_no", length = 100)
    private String externalOrderNo;

    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt;

    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Builder
    public InvestOrderLedger(
            Long orderId,
            Long sweepRequestId,
            Long fxLedgerId,
            UUID investAccountUuid,
            UUID userUuid,
            UUID investUserUuid,
            Long etfId,
            String ticker,
            InvestOrderType orderType,
            String orderMethod,
            String orderCurrency,
            BigDecimal orderAmount,
            BigDecimal orderQuantity,
            BigDecimal referencePrice,
            String orderStatus,
            String externalOrderNo,
            LocalDateTime orderedAt,
            String idempotencyKey
    ) {
        this.orderId = orderId;
        this.sweepRequestId = sweepRequestId;
        this.fxLedgerId = fxLedgerId;
        this.investAccountUuid = investAccountUuid;
        this.userUuid = userUuid;
        this.investUserUuid = investUserUuid;
        this.etfId = etfId;
        this.ticker = ticker;
        this.orderType = orderType;
        this.orderMethod = orderMethod;
        this.orderCurrency = orderCurrency;
        this.orderAmount = orderAmount;
        this.orderQuantity = orderQuantity;
        this.referencePrice = referencePrice;
        this.orderStatus = orderStatus;
        this.externalOrderNo = externalOrderNo;
        this.orderedAt = orderedAt;
        this.idempotencyKey = idempotencyKey;
    }
}
