package com.woorifisa.won_invest_core_server.domain.autoinvest.model;

import com.woorifisa.won_invest_core_server.domain.account.model.InvestAccount;
import com.woorifisa.won_invest_core_server.domain.autoinvest.model.enums.OrderStatus;
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
@Table(
        name = "invest_order_ledger",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_invest_order_ledger_idempotency_key", columnNames = "idempotency_key"),
                @UniqueConstraint(name = "uk_invest_order_ledger_sweep_id", columnNames = "sweep_id")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvestOrderLedger extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @Column(name = "sweep_id", nullable = false)
    private Long sweepId;

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

    @Column(name = "order_currency", nullable = false, length = 10)
    private String orderCurrency;

    @Column(name = "order_amount", nullable = false, precision = 18, scale = 4)
    private BigDecimal orderAmount;

    @Column(name = "order_quantity", nullable = false, precision = 18, scale = 8)
    private BigDecimal orderQuantity;

    @Column(name = "order_price_snapshot", nullable = false, precision = 18, scale = 4)
    private BigDecimal orderPriceSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status", nullable = false, length = 30)
    private OrderStatus orderStatus;

    @Column(name = "external_order_no", length = 100)
    private String externalOrderNo;

    @Column(name = "ordered_at", nullable = false)
    private LocalDateTime orderedAt;

    public static InvestOrderLedger requestedSweepBuy(
            String idempotencyKey,
            Long sweepId,
            InvestAccount account,
            InvestEtfProduct etf,
            BigDecimal quantity,
            BigDecimal amount,
            BigDecimal priceSnapshot,
            String orderCurrency,
            LocalDateTime orderedAt
    ) {
        InvestOrderLedger order = new InvestOrderLedger();
        order.idempotencyKey = idempotencyKey;
        order.sweepId = sweepId;
        order.investAccount = account;
        order.userUuid = account.getUserUuid();
        order.investUserUuid = account.getInvestUser().getInvestUserUuid();
        order.etf = etf;
        order.ticker = etf.getTicker();
        order.orderCurrency = orderCurrency;
        order.orderAmount = amount;
        order.orderQuantity = quantity;
        order.orderPriceSnapshot = priceSnapshot;
        order.orderStatus = OrderStatus.REQUESTED;
        order.orderedAt = orderedAt != null ? orderedAt : LocalDateTime.now();
        return order;
    }

    public void complete() {
        if (this.orderStatus == OrderStatus.COMPLETED) {
            return;
        }

        if (this.orderStatus == OrderStatus.FAILED) {
            throw new IllegalStateException("실패한 주문은 완료 처리할 수 없습니다.");
        }

        this.orderStatus = OrderStatus.COMPLETED;
    }

    public void fail() {
        if (this.orderStatus == OrderStatus.FAILED) {
            return;
        }

        if (this.orderStatus == OrderStatus.COMPLETED) {
            throw new IllegalStateException("완료된 주문은 실패 처리할 수 없습니다.");
        }

        this.orderStatus = OrderStatus.FAILED;
    }

}
