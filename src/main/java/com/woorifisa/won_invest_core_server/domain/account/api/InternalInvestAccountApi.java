package com.woorifisa.won_invest_core_server.domain.account.api;

import com.woorifisa.won_invest_core_server.domain.account.dto.request.CreateInvestAccountRequest;
import com.woorifisa.won_invest_core_server.domain.account.dto.response.CreateInvestAccountResponse;
import com.woorifisa.won_invest_core_server.domain.account.service.InvestAccountService;
import com.woorifisa.won_invest_core_server.global.response.ApiResponse;
import com.woorifisa.won_invest_core_server.global.response.SuccessStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/invest/accounts")
@RequiredArgsConstructor
public class InternalInvestAccountApi {

    private final InvestAccountService investAccountService;

    @PostMapping("/new")
    public ResponseEntity<ApiResponse<CreateInvestAccountResponse>> openNewInvestAccount(
            @Valid @RequestBody CreateInvestAccountRequest request,
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        CreateInvestAccountResponse response =
                investAccountService.openNewInvestAccount(request, authorizationHeader);
        return ApiResponse.of(SuccessStatus.CREATED, "증권계좌 개설이 완료되었습니다.", response);
    }
}