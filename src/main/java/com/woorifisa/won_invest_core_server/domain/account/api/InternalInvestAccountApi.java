package com.woorifisa.won_invest_core_server.domain.account.api;

import com.woorifisa.won_invest_core_server.domain.account.dto.request.CreateInvestAccountRequest;
import com.woorifisa.won_invest_core_server.domain.account.dto.response.CreateInvestAccountResponse;
import com.woorifisa.won_invest_core_server.domain.account.dto.response.InvestAccountEtfDetailsResponse;
import com.woorifisa.won_invest_core_server.domain.account.service.InvestAccountEtfQueryService;
import com.woorifisa.won_invest_core_server.domain.account.service.InvestAccountService;
import com.woorifisa.won_invest_core_server.global.response.ApiResponse;
import com.woorifisa.won_invest_core_server.global.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Internal Account", description = "증권계좌 내부 API")
@RestController
@RequestMapping("/internal/invest/accounts")
@RequiredArgsConstructor
public class InternalInvestAccountApi {

    private final InvestAccountService investAccountService;
    private final InvestAccountEtfQueryService investAccountEtfQueryService;

    @Operation(summary = "신규 증권계좌 개설", description = "채널계 서버로부터 신규 증권계좌 개설 요청을 처리합니다.")
    @PostMapping("/new")
    public ResponseEntity<ApiResponse<CreateInvestAccountResponse>> createInvestAccount(
            @Valid @RequestBody CreateInvestAccountRequest request,
            @RequestHeader("X-User-UUID") UUID userUuid
    ) {
        CreateInvestAccountResponse response =
                investAccountService.openNewInvestAccount(request, userUuid);
        return ResponseEntity
                .status(SuccessStatus.INVEST_ACCOUNT_CREATED.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.INVEST_ACCOUNT_CREATED, response));
    }

    @Operation(
            summary = "보유 ETF 상세 조회",
            description = "내부 채널 서버가 증권계좌의 현재 보유 ETF 목록과 최근 매수 체결 내역을 조회합니다. ACTIVE 상태 계좌만 조회할 수 있습니다."
    )
    @GetMapping("/{accountUuid}/etfs")
    public ResponseEntity<ApiResponse<InvestAccountEtfDetailsResponse>> getAccountEtfDetails(
            @Parameter(description = "호출 서비스 식별자", required = true)
            @RequestHeader("X-Service-ID") String serviceId,
            @Parameter(description = "내부 API 인증 키", required = true)
            @RequestHeader("X-Internal-Api-Key") String internalApiKey,
            @Parameter(description = "원앱 통합 사용자 UUID", required = true)
            @RequestHeader("X-User-UUID") UUID userUuid,
            @Parameter(description = "증권 계좌 UUID", required = true)
            @PathVariable UUID accountUuid
    ) {
        InvestAccountEtfDetailsResponse response =
                investAccountEtfQueryService.getAccountEtfDetails(accountUuid, userUuid);
        return ResponseEntity
                .status(SuccessStatus.INVEST_ACCOUNT_ETF_DETAILS_FETCHED.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.INVEST_ACCOUNT_ETF_DETAILS_FETCHED, response));
    }
}
