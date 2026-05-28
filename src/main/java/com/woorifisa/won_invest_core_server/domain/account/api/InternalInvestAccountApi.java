package com.woorifisa.won_invest_core_server.domain.account.api;

import com.woorifisa.won_invest_core_server.domain.account.dto.request.CreateInvestAccountRequest;
import com.woorifisa.won_invest_core_server.domain.account.dto.response.CreateInvestAccountResponse;
import com.woorifisa.won_invest_core_server.domain.account.service.InvestAccountService;
import com.woorifisa.won_invest_core_server.global.response.ApiResponse;
import com.woorifisa.won_invest_core_server.global.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
}

