package com.woorifisa.won_invest_core_server.domain.etf.api;

import com.woorifisa.won_invest_core_server.domain.etf.dto.request.EtfProductUpsertRequest;
import com.woorifisa.won_invest_core_server.domain.etf.dto.response.EtfProductUpsertResponse;
import com.woorifisa.won_invest_core_server.domain.etf.service.InvestEtfProductService;
import com.woorifisa.won_invest_core_server.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
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
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "ETF 상품 마스터 동기화 성공"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "요청값 검증 실패"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401",
            description = "내부 API 인증 실패"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류"
    )

    // Channel 서버의 ETF 동기화 요청을 받아서
    @PostMapping("/internal/etf-products/sync")
    public ResponseEntity<ApiResponse<EtfProductUpsertResponse>> upsertEtfProduct(
            // Valid - 필수값이 없으면 Controller 진입 단계에서 바로 400으로 막힘
            @Valid @RequestBody EtfProductUpsertRequest request
    ) {
        // Core의 ETF 상품 마스터 저장/수정 Service로 넘기고
        EtfProductUpsertResponse response = investEtfProductService.upsertEtfProduct(request);
        // 처리 결과를 JSON으로 돌려줌
        return ResponseEntity.ok(
                ApiResponse.success("ETF 상품 마스터 동기화가 완료되었습니다.", response)
        );
    }
}