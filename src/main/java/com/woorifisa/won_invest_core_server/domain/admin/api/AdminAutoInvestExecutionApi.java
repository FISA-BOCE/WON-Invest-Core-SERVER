package com.woorifisa.won_invest_core_server.domain.admin.api;

import com.woorifisa.won_invest_core_server.domain.admin.dto.response.AdminAutoInvestExecutionListResponse;
import com.woorifisa.won_invest_core_server.domain.admin.service.AdminAutoInvestExecutionService;
import com.woorifisa.won_invest_core_server.global.response.ApiResponse;
import com.woorifisa.won_invest_core_server.global.response.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/invest/auto-invest")
@Tag(name = "Admin Auto Invest Execution API", description = "관리자 대시보드의 자동투자 실행 현황을 조회하는 API")
public class AdminAutoInvestExecutionApi {

    private final AdminAutoInvestExecutionService adminAutoInvestExecutionService;

    @Operation(
            summary = "자동투자 실행 현황 조회",
            description = "Invest Core의 auto_invest_sweep_ledger 기준으로 자동투자 실행 상태와 상태별 요약 정보를 조회합니다. 환전 상태는 별도 원장이 없어 fxRateSnapshot 기준으로 추론합니다."
    )
    @GetMapping("/executions")
    public ResponseEntity<ApiResponse<AdminAutoInvestExecutionListResponse>> getExecutions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String userUuid,
            @RequestParam(required = false) Long executionId,
            @RequestParam(required = false) Long sweepRequestId,
            @RequestParam(required = false) String ticker,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        AdminAutoInvestExecutionListResponse response = adminAutoInvestExecutionService.getExecutions(
                status,
                userUuid,
                executionId,
                sweepRequestId,
                ticker,
                page,
                size
        );

        return ResponseEntity
                .status(SuccessStatus.ADMIN_AUTO_INVEST_EXECUTIONS_FOUND.getHttpStatus())
                .body(ApiResponse.of(SuccessStatus.ADMIN_AUTO_INVEST_EXECUTIONS_FOUND, response));
    }
}
