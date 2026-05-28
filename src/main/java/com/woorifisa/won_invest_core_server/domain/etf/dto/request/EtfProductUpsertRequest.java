package com.woorifisa.won_invest_core_server.domain.etf.dto.request;

import com.woorifisa.won_invest_core_server.domain.etf.model.type.EtfCurrency;
import com.woorifisa.won_invest_core_server.domain.etf.model.type.EtfProductStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EtfProductUpsertRequest(

        @NotBlank(message = "외부 제공자는 필수입니다.")
        @Size(max = 50, message = "외부 제공자는 50자를 초과할 수 없습니다.")
        String externalProvider,

        @Size(max = 100, message = "외부 ETF ID는 100자를 초과할 수 없습니다.")
        String externalEtfId,

        @NotBlank(message = "ETF 티커는 필수입니다.")
        @Size(max = 20, message = "ETF 티커는 20자를 초과할 수 없습니다.")
        String ticker,

        @Size(max = 20, message = "ISIN은 20자를 초과할 수 없습니다.")
        String isin,

        @NotBlank(message = "ETF 상품명은 필수입니다.")
        @Size(max = 100, message = "ETF 상품명은 100자를 초과할 수 없습니다.")
        String etfName,

        @Size(max = 30, message = "시장은 30자를 초과할 수 없습니다.")
        String market,

        @NotNull(message = "통화는 필수입니다.")
        EtfCurrency currency,

        @NotNull(message = "상품 상태는 필수입니다.")
        EtfProductStatus productStatus,
        Boolean isFractionalAvailable,
        Boolean isTradeAvailable
) {
}