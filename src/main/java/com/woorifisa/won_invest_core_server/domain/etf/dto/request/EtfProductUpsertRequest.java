package com.woorifisa.won_invest_core_server.domain.etf.dto.request;

import com.woorifisa.won_invest_core_server.domain.etf.model.type.EtfCurrency;
import com.woorifisa.won_invest_core_server.domain.etf.model.type.EtfProductStatus;

public record EtfProductUpsertRequest(
        String externalProvider,
        String externalEtfId,
        String ticker,
        String isin,
        String etfName,
        String market,
        EtfCurrency currency,
        EtfProductStatus productStatus,
        Boolean isFractionalAvailable,
        Boolean isTradeAvailable
) {
}