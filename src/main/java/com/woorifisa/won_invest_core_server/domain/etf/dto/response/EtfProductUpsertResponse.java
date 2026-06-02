package com.woorifisa.won_invest_core_server.domain.etf.dto.response;

import com.woorifisa.won_invest_core_server.domain.etf.model.InvestEtfProduct;
import com.woorifisa.won_invest_core_server.domain.etf.model.enums.EtfCurrency;
import com.woorifisa.won_invest_core_server.domain.etf.model.enums.EtfProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ETF 상품 마스터 동기화 응답")
public record EtfProductUpsertResponse(
        @Schema(description = "ETF 내부 ID", example = "1")
        Long etfId,

        @Schema(description = "외부 제공자", example = "KIS")
        String externalProvider,

        @Schema(description = "외부 ETF 식별자", example = "VOO")
        String externalEtfId,

        @Schema(description = "ETF 티커", example = "VOO")
        String ticker,

        @Schema(description = "ETF 상품명", example = "Vanguard S&P 500 ETF")
        String etfName,

        @Schema(description = "거래 시장", example = "NYSE")
        String market,

        @Schema(description = "ETF 거래 통화", example = "USD")
        EtfCurrency currency,

        @Schema(description = "ETF 상품 상태", example = "ACTIVE")
        EtfProductStatus productStatus,

        @Schema(description = "소수점 매수 가능 여부", example = "true")
        Boolean isFractionalAvailable,

        @Schema(description = "거래 가능 여부", example = "true")
        Boolean isTradeAvailable
) {

    public static EtfProductUpsertResponse from(InvestEtfProduct product) {
        return new EtfProductUpsertResponse(
                product.getEtfId(),
                product.getExternalProvider(),
                product.getExternalEtfId(),
                product.getTicker(),
                product.getEtfName(),
                product.getMarket(),
                product.getCurrency(),
                product.getProductStatus(),
                product.getIsFractionalAvailable(),
                product.getIsTradeAvailable()
        );
    }
}
