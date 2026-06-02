package com.woorifisa.won_invest_core_server.domain.etf.dto.request;

import com.woorifisa.won_invest_core_server.domain.etf.model.enums.EtfCurrency;
import com.woorifisa.won_invest_core_server.domain.etf.model.enums.EtfProductStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "ETF 상품 마스터 동기화 요청")
public record EtfProductUpsertRequest(

        @Schema(description = "외부 제공자", example = "KIS")
        @NotBlank(message = "외부 제공자는 필수입니다.")
        @Size(max = 50, message = "외부 제공자는 50자를 초과할 수 없습니다.")
        String externalProvider,

        @Schema(description = "외부 ETF 식별자", example = "VOO")
        @Size(max = 100, message = "외부 ETF ID는 100자를 초과할 수 없습니다.")
        String externalEtfId,

        @Schema(description = "ETF 티커", example = "VOO")
        @NotBlank(message = "ETF 티커는 필수입니다.")
        @Size(max = 20, message = "ETF 티커는 20자를 초과할 수 없습니다.")
        String ticker,

        @Schema(description = "ISIN", example = "US9229083632")
        @Size(max = 20, message = "ISIN은 20자를 초과할 수 없습니다.")
        String isin,

        @Schema(description = "ETF 상품명", example = "Vanguard S&P 500 ETF")
        @NotBlank(message = "ETF 상품명은 필수입니다.")
        @Size(max = 100, message = "ETF 상품명은 100자를 초과할 수 없습니다.")
        String etfName,

        @Schema(description = "거래 시장", example = "NYSE")
        @Size(max = 30, message = "시장은 30자를 초과할 수 없습니다.")
        String market,

        @Schema(description = "통화", example = "USD")
        @NotNull(message = "통화는 필수입니다.")
        EtfCurrency currency,

        @Schema(description = "상품 상태", example = "ACTIVE")
        @NotNull(message = "상품 상태는 필수입니다.")
        EtfProductStatus productStatus,

        @Schema(description = "소수점 매수 가능 여부", example = "true")
        Boolean isFractionalAvailable,

        @Schema(description = "거래 가능 여부", example = "true")
        Boolean isTradeAvailable
) {
}