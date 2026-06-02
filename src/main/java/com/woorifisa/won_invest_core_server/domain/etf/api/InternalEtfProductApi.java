package com.woorifisa.won_invest_core_server.domain.etf.api;

import com.woorifisa.won_invest_core_server.domain.etf.dto.request.EtfProductUpsertRequest;
import com.woorifisa.won_invest_core_server.domain.etf.dto.response.EtfProductUpsertResponse;
import com.woorifisa.won_invest_core_server.domain.etf.service.InvestEtfProductService;
import com.woorifisa.won_invest_core_server.global.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Internal ETF Product API", description = "내부 ETF 상품 마스터 동기화 API")
@RequiredArgsConstructor
@RestController
public class InternalEtfProductApi {

    private final InvestEtfProductService investEtfProductService;

    @Operation(
            summary = "ETF 상품 마스터 동기화",
            description = """
                    외부 API 또는 증권 모의 API에서 수집한 ETF 상품 정보를 Invest-Core 상품 마스터에 등록하거나 갱신합니다.
                    
                    - externalProvider + externalEtfId 기준으로 우선 조회합니다.
                    - externalEtfId가 없거나 기존 데이터가 없는 경우 externalProvider + ticker 기준으로 조회합니다.
                    - 기존 상품이 있으면 갱신하고, 없으면 신규 등록합니다.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "ETF 상품 마스터 동기화 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "요청값 검증 실패"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "내부 API 인증 실패"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류"
            )
    })

    // Channel 서버의 ETF 동기화 요청을 받아서
    @PostMapping("/internal/etf-products/sync")
    public ResponseEntity<com.woorifisa.won_invest_core_server.global.response.ApiResponse<EtfProductUpsertResponse>> upsertEtfProduct(
            @Valid @RequestBody EtfProductUpsertRequest request
    ) {
        EtfProductUpsertResponse response = investEtfProductService.upsertEtfProduct(request);
        return ResponseEntity
                .status(SuccessStatus.ETF_PRODUCT_SYNCED.getHttpStatus())
                .body(com.woorifisa.won_invest_core_server.global.response.ApiResponse.of(
                        SuccessStatus.ETF_PRODUCT_SYNCED,
                        response
                ));

    }
}
