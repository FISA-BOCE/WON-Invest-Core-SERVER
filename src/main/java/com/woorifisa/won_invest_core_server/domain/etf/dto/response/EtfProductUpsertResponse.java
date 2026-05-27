package com.woorifisa.won_invest_core_server.domain.etf.dto.response;

import com.woorifisa.won_invest_core_server.domain.etf.model.InvestEtfProduct;
import com.woorifisa.won_invest_core_server.domain.etf.model.type.EtfCurrency;
import com.woorifisa.won_invest_core_server.domain.etf.model.type.EtfProductStatus;

public record EtfProductUpsertResponse(
        Long etfId,
        String externalProvider,
        String externalEtfId,
        String ticker,
        String etfName,
        String market,
        EtfCurrency currency,
        EtfProductStatus productStatus,
        Boolean isFractionalAvailable,
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