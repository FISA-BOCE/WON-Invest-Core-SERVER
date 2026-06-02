// 외부 API 기준 ETF 원천 정보

package com.woorifisa.won_invest_core_server.domain.etf.model;

import com.woorifisa.won_invest_core_server.domain.etf.model.enums.EtfCurrency;
import com.woorifisa.won_invest_core_server.domain.etf.model.enums.EtfProductStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "invest_etf_product",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_invest_etf_product_provider_external_id",
                        columnNames = {"external_provider", "external_etf_id"}
                ),
                @UniqueConstraint(
                        name = "uk_invest_etf_product_provider_ticker",
                        columnNames = {"external_provider", "ticker"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvestEtfProduct {

    // ETF 상품 마스터의 기준 식별자
    // Core 계정계에서 AUTO_INCREMENT로 생성하고, Channel 서버는 Core에서 반환받은 etf_id를 서비스 노출 ETF ID로 사용
    // 추후 외부 API 상품코드 체계 또는 내부 상품 마스터 정책이 확정되면, 별도 시퀀스/채번 정책을 도입하여 etf_id 생성 방식을 보강할 수 있다.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "etf_id", nullable = false)
    private Long etfId;

    // 어느 외부 API에서 받아온 ETF인지 - "KIS" 등
    @Column(name = "external_provider", nullable = false, length = 50)
    private String externalProvider;

    // 외부 API에서 쓰는 ETF 고유 ID
    @Column(name = "external_etf_id", length = 100)
    private String externalEtfId;

    //ex) VOO, QQQ, SCHD, SPY 등
    @Column(name = "ticker", nullable = false, length = 20)
    private String ticker;

    // 국제 증권 식별 코드 ISIN - 모든 API에서 항상 주는 값은 아닐 수 있어서 nullable
    @Column(name = "isin", length = 20)
    private String isin;

    //ex) Vanguard S&P 500 ETF, Invesco QQQ Trust 등
    @Column(name = "etf_name", nullable = false, length = 100)
    private String etfName;

    //거래 시장 - ex) NYSE, NASDAQ, AMEX 등
    @Column(name = "market", length = 30)
    private String market;

    // ETF 거래 통화 - USD, KRW, CAD, EUR +a 추가 가능
    @Enumerated(EnumType.STRING)
    @Column(name = "currency", nullable = false, length = 10)
    private EtfCurrency currency;

    //ACTIVE, INACTIVE, SUSPENDED, DELISTED
    @Enumerated(EnumType.STRING)
    @Column(name = "product_status", nullable =  false, length = 30)
    private EtfProductStatus productStatus;

    // 소수점 매수 가능 여부
    @Column(name = "is_fractional_available", nullable = false)
    private Boolean isFractionalAvailable;

    // 현재 이 ETF가 거래 가능한지
    @Column(name = "is_trade_available", nullable = false)
    private Boolean isTradeAvailable;

    // 외부 API에서 이 ETF 정보를 마지막으로 가져온 시간
    @Column(name = "last_synced_at", nullable = false)
    private LocalDateTime lastSyncedAt;

    @Builder
    public InvestEtfProduct(
            Long etfId,
            String externalProvider,
            String externalEtfId,
            String ticker,
            String isin,
            String etfName,
            String market,
            EtfCurrency currency,
            EtfProductStatus productStatus,
            Boolean isFractionalAvailable,
            Boolean isTradeAvailable,
            LocalDateTime lastSyncedAt
    ) {
        this.etfId = etfId;
        this.externalProvider = externalProvider;
        this.externalEtfId = externalEtfId;
        this.ticker = ticker;
        this.isin = isin;
        this.etfName = etfName;
        this.market = market;
        this.currency = currency != null ? currency : EtfCurrency.USD;  // currency 값이 있으면 그대로 쓰고 -> 없으면 USD
        this.productStatus = productStatus != null ? productStatus : EtfProductStatus.INACTIVE; // productStatus 누락되면 -> INACTIVE로 보정
        this.isFractionalAvailable = isFractionalAvailable != null ? isFractionalAvailable : false;
        this.isTradeAvailable = isTradeAvailable != null ? isTradeAvailable : false;
        this.lastSyncedAt = lastSyncedAt != null ? lastSyncedAt : LocalDateTime.now();
    }

    //기존 ETF 상품 정보를 갱신하는 메서드
    // etfId(내부 PK라서 바뀌면 안됨), externalProvider(출처 제공자 고정), ticker(ETF 식별값)는 업데이트 대상에서 빠짐
    public void updateProductInfo(
            String externalEtfId,
            String isin,
            String etfName,
            String market,
            EtfCurrency currency,
            EtfProductStatus productStatus,
            Boolean isFractionalAvailable,
            Boolean isTradeAvailable,
            LocalDateTime lastSyncedAt
    ) {
        this.externalEtfId = externalEtfId;
        this.isin = isin;
        this.etfName = etfName;
        this.market = market;
        this.currency = currency != null ? currency : EtfCurrency.USD;
        this.productStatus = productStatus != null ? productStatus : EtfProductStatus.INACTIVE;
        this.isFractionalAvailable = isFractionalAvailable != null ? isFractionalAvailable : false;
        this.isTradeAvailable = isTradeAvailable != null ? isTradeAvailable : false;
        this.lastSyncedAt = lastSyncedAt != null ? lastSyncedAt : LocalDateTime.now();
    }

    // 자동투자 대상으로 제공 가능한지 판단하는 비즈니스 메서드
    // 조건 1. 거래 가능해야 함         isTradeAvailable == true
    // 조건 2. 소수점 매수 가능해야 함    isFractionalAvailable == true
    // 조건 3. 통화가 USD여야 함        currency.equals("USD")
    public boolean canProvideAutoInvestment() {
        return Boolean.TRUE.equals(isTradeAvailable)
                && Boolean.TRUE.equals(isFractionalAvailable)
                && EtfCurrency.USD.equals(currency)
                && EtfProductStatus.ACTIVE.equals(productStatus);
    }
}
